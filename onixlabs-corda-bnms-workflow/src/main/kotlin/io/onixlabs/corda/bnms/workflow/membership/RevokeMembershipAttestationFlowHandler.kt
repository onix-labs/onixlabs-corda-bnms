package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.identity.framework.workflow.FINALIZING
import io.onixlabs.corda.identity.framework.workflow.currentStep
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
    private val statesToRecord: StatesToRecord = StatesToRecord.ALL_VISIBLE,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(FINALIZING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(FINALIZING)
        return subFlow(ReceiveFinalityFlow(session, expectedTransactionId, statesToRecord))
    }
}