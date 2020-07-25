package io.onixlabs.corda.bnms.workflow.relationship

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestationContract
import io.onixlabs.corda.bnms.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class RevokeRelationshipAttestationFlow(
    private val attestation: StateAndRef<RelationshipAttestation>,
    private val sessions: Set<FlowSession>,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(INITIALIZING, GENERATING, VERIFYING, SIGNING, FINALIZING)

        private const val FLOW_VERSION_1 = 1
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(INITIALIZING)
        checkSufficientSessions(attestation.state.data, sessions)

        val transaction = transaction(attestation.state.notary) {
            addInputState(attestation)
            addCommand(RelationshipAttestationContract.Revoke, ourIdentity.owningKey)
        }

        val signedTransaction = verifyAndSign(transaction)
        return finalize(signedTransaction, sessions)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val attestation: StateAndRef<RelationshipAttestation>
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object REVOKING : ProgressTracker.Step("Revoking relationship attestation.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(REVOKING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(REVOKING)
            val sessions = initiateFlows(attestation.state.data.attestees)
            return subFlow(
                RevokeRelationshipAttestationFlow(
                    attestation,
                    sessions,
                    REVOKING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    internal class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : Step("Observing relationship attestation revocation.") {
                override fun childProgressTracker() = IssueRelationshipAttestationFlowHandler.tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(IssueRelationshipAttestationFlowHandler(session, null, OBSERVING.childProgressTracker()))
        }
    }
}