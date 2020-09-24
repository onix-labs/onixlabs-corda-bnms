package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationContract
import io.onixlabs.corda.bnms.workflow.checkSufficientSessions
import io.onixlabs.corda.identity.framework.contract.EvolvableAttestationContract
import io.onixlabs.corda.identity.framework.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class AmendMembershipAttestationFlow(
    private val oldAttestation: StateAndRef<MembershipAttestation>,
    private val newAttestation: MembershipAttestation,
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
            addOutputState(newAttestation, MembershipAttestationContract.ID)
            addReferenceState(newAttestation.pointer.resolve(serviceHub).referenced())
            addCommand(EvolvableAttestationContract.Amend, newAttestation.attestor.owningKey)
        }

        val signedTransaction = verifyAndSign(transaction, newAttestation.attestor.owningKey)
        return finalize(signedTransaction, sessions)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val oldAttestation: StateAndRef<MembershipAttestation>,
        private val newAttestation: MembershipAttestation,
        private val observers: Set<Party> = emptySet()
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object AMENDING : Step("Amending membership attestation.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(AMENDING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(AMENDING)
            val sessions = initiateFlows(newAttestation.attestees + observers)

            return subFlow(
                AmendMembershipAttestationFlow(
                    oldAttestation,
                    newAttestation,
                    sessions,
                    AMENDING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    private class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : Step("Observing membership attestation amendment.") {
                override fun childProgressTracker() = AmendMembershipAttestationFlowHandler.tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(
                AmendMembershipAttestationFlowHandler(
                    session,
                    progressTracker = OBSERVING.childProgressTracker()
                )
            )
        }
    }
}