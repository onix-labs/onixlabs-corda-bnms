package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationContract
import io.onixlabs.corda.bnms.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class AmendMembershipAttestationFlow(
    private val oldAttestation: StateAndRef<MembershipAttestation>,
    private val newAttestation: MembershipAttestation,
    private val notary: Party,
    private val sessions: Set<FlowSession>,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(
            INITIALIZING,
            GENERATING,
            VERIFYING,
            SIGNING,
            FINALIZING
        )

        private const val FLOW_VERSION_1 = 1
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(INITIALIZING)
        checkSufficientSessions(newAttestation, sessions)

        val transaction = transaction(notary) {
            addInputState(oldAttestation)
            addOutputState(newAttestation, MembershipAttestationContract.ID)
            addReferenceState(newAttestation.pointer.resolve(serviceHub).referenced())
            addCommand(MembershipAttestationContract.Amend, newAttestation.attestor.owningKey)
        }

        val signedTransaction = verifyAndSign(transaction)

        return finalize(signedTransaction, sessions)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val oldAttestation: StateAndRef<MembershipAttestation>,
        private val newAttestation: MembershipAttestation,
        private val notary: Party? = null,
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
                    notary ?: firstNotary,
                    sessions,
                    AMENDING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    internal class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : Step("Observing membership attestation.") {
                override fun childProgressTracker() = IssueMembershipAttestationFlowHandler.tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(AmendMembershipAttestationFlowHandler(session, null, OBSERVING.childProgressTracker()))
        }
    }
}