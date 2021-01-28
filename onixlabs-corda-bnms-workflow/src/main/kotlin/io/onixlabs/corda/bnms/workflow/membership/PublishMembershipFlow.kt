package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.v1.contract.membership.Membership
import io.onixlabs.corda.core.workflow.currentStep
import io.onixlabs.corda.core.workflow.findTransaction
import io.onixlabs.corda.core.workflow.initiateFlows
import io.onixlabs.corda.identityframework.workflow.INITIALIZING
import io.onixlabs.corda.identityframework.workflow.SENDING
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

class PublishMembershipFlow(
    private val membership: StateAndRef<Membership>,
    private val sessions: Set<FlowSession>,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(INITIALIZING, SENDING)

        private const val FLOW_VERSION_1 = 1
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(INITIALIZING)
        val transaction = findTransaction(membership)

        currentStep(SENDING)
        sessions.forEach { subFlow(SendTransactionFlow(it, transaction)) }

        return transaction
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val membership: StateAndRef<Membership>,
        private val observers: Set<Party>
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object PUBLISHING : ProgressTracker.Step("Publishing membership transaction.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(PUBLISHING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(PUBLISHING)
            return subFlow(
                PublishMembershipFlow(
                    membership,
                    initiateFlows(observers),
                    PUBLISHING.childProgressTracker()
                )
            )
        }
    }
}
