package io.onixlabs.corda.bnms.workflow.relationship

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipContract
import io.onixlabs.corda.bnms.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class AmendRelationshipFlow(
    private val oldRelationship: StateAndRef<Relationship>,
    private val newRelationship: Relationship,
    private val sessions: Set<FlowSession>,
    private val checkMembership: Boolean = false,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(INITIALIZING, GENERATING, VERIFYING, SIGNING, COUNTERSIGNING, FINALIZING)

        private const val FLOW_VERSION_1 = 1
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(INITIALIZING)
        checkSufficientSessions(newRelationship, sessions)
        sessions.forEach { it.send(checkMembership) }

        if (checkMembership) {
            val counterparties = filterCounterpartyIdentities(newRelationship.participantSettings.keys)
            checkMembershipsAndAttestations(newRelationship, counterparties)
        }

        val transaction = transaction(oldRelationship.state.notary) {
            addInputState(oldRelationship)
            addOutputState(newRelationship, RelationshipContract.ID)
            addCommand(RelationshipContract.Amend, newRelationship.participants.map { it.owningKey })
        }

        val partiallySignedTransaction = verifyAndSign(transaction)
        val fullySignedTransaction = countersign(partiallySignedTransaction, sessions)
        return finalize(fullySignedTransaction, sessions)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val oldRelationship: StateAndRef<Relationship>,
        private val newRelationship: Relationship,
        private val checkMembership: Boolean = false
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object AMENDING : Step("Amending relationship.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(AMENDING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(AMENDING)
            val sessions = initiateFlows(newRelationship.participants)

            return subFlow(
                AmendRelationshipFlow(
                    oldRelationship,
                    newRelationship,
                    sessions,
                    checkMembership,
                    AMENDING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    internal class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : Step("Observing relationship amendment.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(AmendRelationshipFlowHandler(session, OBSERVING.childProgressTracker()))
        }
    }
}