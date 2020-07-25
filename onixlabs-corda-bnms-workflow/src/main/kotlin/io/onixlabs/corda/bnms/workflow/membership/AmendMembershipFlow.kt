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

class AmendMembershipFlow(
    private val oldMembership: StateAndRef<Membership>,
    private val newMembership: Membership,
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
        checkMembershipExists(newMembership)
        checkSufficientSessions(newMembership, sessions)

        val transaction = transaction(notary) {
            addInputState(oldMembership)
            addOutputState(newMembership, MembershipContract.ID)
            addCommand(MembershipContract.Amend, ourIdentity.owningKey)
        }

        val signedTransaction = verifyAndSign(transaction)

        return finalize(signedTransaction, sessions)
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val oldMembership: StateAndRef<Membership>,
        private val newMembership: Membership,
        private val notary: Party? = null,
        private val observers: Set<Party> = emptySet()
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object AMENDING : Step("Amending membership.") {
                override fun childProgressTracker() =
                    tracker()
            }
        }

        override val progressTracker = ProgressTracker(AMENDING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(AMENDING)
            val sessions = initiateFlows(observers, newMembership.network.operator)

            return subFlow(
                AmendMembershipFlow(
                    oldMembership,
                    newMembership,
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
            object OBSERVING : Step("Observing membership amendment.") {
                override fun childProgressTracker() =
                    AmendMembershipFlowHandler.tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(
                AmendMembershipFlowHandler(
                    session,
                    null,
                    OBSERVING.childProgressTracker()
                )
            )
        }
    }
}