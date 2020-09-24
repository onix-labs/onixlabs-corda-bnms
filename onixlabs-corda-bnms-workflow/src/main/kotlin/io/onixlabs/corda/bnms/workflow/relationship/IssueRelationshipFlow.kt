package io.onixlabs.corda.bnms.workflow.relationship

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipContract
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockContract
import io.onixlabs.corda.bnms.workflow.checkMembershipsAndAttestations
import io.onixlabs.corda.bnms.workflow.checkSufficientSessions
import io.onixlabs.corda.bnms.workflow.filterCounterpartyIdentities
import io.onixlabs.corda.identity.framework.workflow.*
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class IssueRelationshipFlow(
    private val relationship: Relationship,
    private val notary: Party,
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
        checkSufficientSessions(relationship, sessions)
        sessions.forEach { it.send(checkMembership) }

        if (checkMembership) {
            val counterparties = filterCounterpartyIdentities(relationship.participants)
            checkMembershipsAndAttestations(relationship, counterparties)
        }

        val transaction = transaction(notary) {
            addOutputState(relationship, RelationshipContract.ID)
            addCommand(RelationshipContract.Issue, relationship.participants.map { it.owningKey })
            relationship.createRevocationLocks().forEach { addOutputState(it) }
            addCommand(RevocationLockContract.Create, relationship.participants.map { it.owningKey })
        }

        val partiallySignedTransaction = verifyAndSign(transaction, ourIdentity.owningKey)
        val fullySignedTransaction = countersign(partiallySignedTransaction, sessions)
        return finalize(fullySignedTransaction, sessions)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val relationship: Relationship,
        private val notary: Party? = null,
        private val checkMembership: Boolean = false
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object ISSUING : Step("Issuing relationship.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(ISSUING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(ISSUING)
            val sessions = initiateFlows(relationship.participants)

            return subFlow(
                IssueRelationshipFlow(
                    relationship,
                    notary ?: preferredNotary,
                    sessions,
                    checkMembership,
                    ISSUING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    private class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : Step("Observing relationship issuance.") {
                override fun childProgressTracker() = IssueRelationshipFlowHandler.tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(IssueRelationshipFlowHandler(session, OBSERVING.childProgressTracker()))
        }
    }
}