package io.onixlabs.corda.bnms.workflow.revocation

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockContract
import io.onixlabs.corda.identity.framework.workflow.*
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

@StartableByRPC
@StartableByService
class CreateRevocationLockFlow(
    private val revocationLock: RevocationLock<*>,
    private val notary: Party? = null,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(GENERATING, VERIFYING, SIGNING, FINALIZING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        val transaction = transaction(notary ?: preferredNotary) {
            addOutputState(revocationLock, RevocationLockContract.ID)
            addCommand(RevocationLockContract.Create, revocationLock.owner.owningKey)
        }

        val signedTransaction = verifyAndSign(transaction, revocationLock.owner.owningKey)
        return finalize(signedTransaction)
    }
}