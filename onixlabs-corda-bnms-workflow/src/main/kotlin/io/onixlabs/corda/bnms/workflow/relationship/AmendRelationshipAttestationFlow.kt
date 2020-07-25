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

class AmendRelationshipAttestationFlow(
    private val oldAttestation: StateAndRef<RelationshipAttestation>,
    private val newAttestation: RelationshipAttestation,
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
        checkSufficientSessions(newAttestation, sessions)

        val transaction = transaction(oldAttestation.state.notary) {
            addInputState(oldAttestation)
            addOutputState(newAttestation, RelationshipAttestationContract.ID)
            addReferenceState(newAttestation.pointer.resolve(serviceHub).referenced())
            addCommand(RelationshipAttestationContract.Amend, ourIdentity.owningKey)
        }

        val signedTransaction = verifyAndSign(transaction)
        return finalize(signedTransaction, sessions)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val oldAttestation: StateAndRef<RelationshipAttestation>,
        private val newAttestation: RelationshipAttestation
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object AMENDING : ProgressTracker.Step("Amending relationship attestation.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(AMENDING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(AMENDING)
            val sessions = initiateFlows(newAttestation.participants)
            return subFlow(
                AmendRelationshipAttestationFlow(
                    oldAttestation,
                    newAttestation,
                    sessions,
                    AMENDING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    internal class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : Step("Observing relationship attestation amendment.") {
                override fun childProgressTracker() = AmendRelationshipAttestationFlowHandler.tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(AmendRelationshipAttestationFlowHandler(session, null, OBSERVING.childProgressTracker()))
        }
    }
}