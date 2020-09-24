package io.onixlabs.corda.bnms.workflow.relationship

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipContract
import io.onixlabs.corda.bnms.workflow.checkSufficientSessions
import io.onixlabs.corda.identity.framework.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class RevokeRelationshipFlow(
    private val relationship: StateAndRef<Relationship>,
    private val sessions: Set<FlowSession>,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        private const val FLOW_VERSION_1 = 1

        @JvmStatic
        fun tracker() = ProgressTracker(INITIALIZING, GENERATING, VERIFYING, SIGNING, COUNTERSIGNING, FINALIZING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(INITIALIZING)
        checkSufficientSessions(relationship.state.data, sessions)

        val transaction = transaction(relationship.state.notary) {
            addInputState(relationship)
            addCommand(RelationshipContract.Revoke, relationship.state.data.participants.map { it.owningKey })
        }

        val partiallySignedTransaction = verifyAndSign(transaction, ourIdentity.owningKey)
        val fullySignedTransaction = countersign(partiallySignedTransaction, sessions)
        return finalize(fullySignedTransaction, sessions)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val relationship: StateAndRef<Relationship>
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object REVOKING : Step("Revoking relationship.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(REVOKING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(REVOKING)
            val sessions = initiateFlows(relationship.state.data.participants)

            return subFlow(
                RevokeRelationshipFlow(
                    relationship,
                    sessions,
                    REVOKING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    private class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : Step("Observing relationship revocation.") {
                override fun childProgressTracker() = RevokeRelationshipFlowHandler.tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(RevokeRelationshipFlowHandler(session, OBSERVING.childProgressTracker()))
        }
    }
}