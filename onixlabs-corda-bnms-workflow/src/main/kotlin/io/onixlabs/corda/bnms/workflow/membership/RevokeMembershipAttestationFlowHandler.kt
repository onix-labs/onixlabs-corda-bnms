package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.workflow.COUNTERFINALIZING
import io.onixlabs.corda.bnms.workflow.currentStep
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.ReceiveFinalityFlow
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

class RevokeMembershipAttestationFlowHandler(
    private val session: FlowSession,
    private val expectedTransactionId: SecureHash? = null,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(COUNTERFINALIZING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(COUNTERFINALIZING)
        return subFlow(ReceiveFinalityFlow(session, expectedTransactionId, StatesToRecord.ALL_VISIBLE))
    }
}