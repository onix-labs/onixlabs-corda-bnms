package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipContract
import io.onixlabs.corda.bnms.workflow.*
import io.onixlabs.corda.bnms.workflow.FINALIZING
import io.onixlabs.corda.bnms.workflow.GENERATING
import io.onixlabs.corda.bnms.workflow.INITIALIZING
import io.onixlabs.corda.bnms.workflow.SIGNING
import io.onixlabs.corda.bnms.workflow.VERIFYING
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class RevokeMembershipFlow(
    private val membership: StateAndRef<Membership>,
    private val notary: Party,
    private val sessions: Set<FlowSession> = emptySet(),
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
        checkSufficientSessions(membership.state.data, sessions)

        val transaction = transaction(notary) {
            addInputState(membership)
            addCommand(MembershipContract.Revoke, ourIdentity.owningKey)
        }

        val signedTransaction = verifyAndSign(transaction)

        return finalize(signedTransaction, sessions)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val membership: StateAndRef<Membership>,
        private val notary: Party? = null,
        private val observers: Set<Party> = emptySet()
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object REVOKING : Step("Revoking membership.") {
                override fun childProgressTracker() =
                    tracker()
            }
        }

        override val progressTracker = ProgressTracker(REVOKING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(REVOKING)
            val sessions = initiateFlows(observers, membership.state.data.network.operator)

            return subFlow(
                RevokeMembershipFlow(
                    membership,
                    notary ?: firstNotary,
                    sessions,
                    REVOKING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    internal class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : Step("Observing membership revocation.") {
                override fun childProgressTracker() =
                    RevokeMembershipFlowHandler.tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(
                RevokeMembershipFlowHandler(
                    session,
                    null,
                    OBSERVING.childProgressTracker()
                )
            )
        }
    }
}