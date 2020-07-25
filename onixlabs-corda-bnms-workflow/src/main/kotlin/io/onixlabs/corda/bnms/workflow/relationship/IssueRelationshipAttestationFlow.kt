package io.onixlabs.corda.bnms.workflow.relationship

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestationContract
import io.onixlabs.corda.bnms.workflow.*
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class IssueRelationshipAttestationFlow(
    private val attestation: RelationshipAttestation,
    private val notary: Party,
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
        checkSufficientSessions(attestation, sessions)

        val transaction = transaction(notary) {
            addOutputState(attestation, RelationshipAttestationContract.ID)
            addReferenceState(attestation.pointer.resolve(serviceHub).referenced())
            addCommand(RelationshipAttestationContract.Issue, ourIdentity.owningKey)
        }

        val signedTransaction = verifyAndSign(transaction)
        return finalize(signedTransaction, sessions)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val attestation: RelationshipAttestation,
        private val notary: Party? = null
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object ISSUING : Step("Issuing relationship attestation.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(ISSUING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(ISSUING)
            val sessions = initiateFlows(attestation.participants)
            return subFlow(
                IssueRelationshipAttestationFlow(
                    attestation,
                    notary ?: firstNotary,
                    sessions,
                    ISSUING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    internal class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : Step("Observing relationship attestation issuance.") {
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