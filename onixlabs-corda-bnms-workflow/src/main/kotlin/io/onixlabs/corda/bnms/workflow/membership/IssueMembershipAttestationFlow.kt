package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationContract
import io.onixlabs.corda.bnms.workflow.checkSufficientSessions
import io.onixlabs.corda.identity.framework.contract.EvolvableAttestationContract
import io.onixlabs.corda.identity.framework.workflow.*
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class IssueMembershipAttestationFlow(
    private val attestation: MembershipAttestation,
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
            addOutputState(attestation)
            addReferenceState(attestation.pointer.resolve(serviceHub).referenced())
            addCommand(EvolvableAttestationContract.Issue, attestation.attestor.owningKey)
        }

        val signedTransaction = verifyAndSign(transaction, attestation.attestor.owningKey)
        return finalize(signedTransaction, sessions)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val attestation: MembershipAttestation,
        private val notary: Party? = null,
        private val observers: Set<Party> = emptySet()
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object ISSUING : Step("Issuing membership attestation.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(ISSUING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(ISSUING)
            val sessions = initiateFlows(attestation.participants + observers)

            return subFlow(
                IssueMembershipAttestationFlow(
                    attestation,
                    notary ?: preferredNotary,
                    sessions,
                    ISSUING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    private class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : Step("Observing membership attestation issuance.") {
                override fun childProgressTracker() = IssueMembershipAttestationFlowHandler.tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(
                IssueMembershipAttestationFlowHandler(
                    session,
                    progressTracker = OBSERVING.childProgressTracker()
                )
            )
        }
    }
}