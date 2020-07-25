package io.onixlabs.corda.bnms.workflow.revocation

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockContract
import io.onixlabs.corda.bnms.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

@StartableByRPC
@StartableByService
class DeleteRevocationLockFlow(
    private val revocationLock: StateAndRef<RevocationLock<*>>,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(GENERATING, VERIFYING, SIGNING, FINALIZING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        val transaction = transaction(revocationLock.state.notary) {
            addInputState(revocationLock)
            addCommand(RevocationLockContract.Delete, ourIdentity.owningKey)
        }

        val signedTransaction = verifyAndSign(transaction)
        return finalize(signedTransaction)
    }
}