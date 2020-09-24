package io.onixlabs.corda.bnms.workflow.relationship

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockStatus
import io.onixlabs.corda.bnms.workflow.revocation.DeleteRevocationLockFlow
import io.onixlabs.corda.bnms.workflow.revocation.FindRevocationLockFlow
import io.onixlabs.corda.identity.framework.workflow.FINALIZING
import io.onixlabs.corda.identity.framework.workflow.SIGNING
import io.onixlabs.corda.identity.framework.workflow.currentStep
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class RevokeRelationshipFlowHandler(
    private val session: FlowSession,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(SIGNING, FINALIZING, DELETING)

        private object DELETING : Step("Deleting revocation lock.")
    }

    @Suspendable
    override fun call(): SignedTransaction {
        var revocationLock: StateAndRef<RevocationLock<Relationship>>? = null

        currentStep(SIGNING)
        val transaction = subFlow(object : SignTransactionFlow(session) {
            override fun checkTransaction(stx: SignedTransaction) {
                val relationshipStateRef = stx.tx.inputs.singleOrNull()
                    ?: throw FlowException("Failed to obtain a single state reference from the transaction.")

                val relationship = serviceHub.toStateAndRef<Relationship>(relationshipStateRef)
                revocationLock = subFlow(FindRevocationLockFlow(ourIdentity, relationship.state.data))

                if (revocationLock?.state?.data?.status == RevocationLockStatus.LOCKED) {
                    throw FlowException("Revocation of this relationship is locked by counter-party: $ourIdentity")
                }
            }
        })

        currentStep(FINALIZING)
        val finalizedTransaction = subFlow(ReceiveFinalityFlow(session, transaction.id, StatesToRecord.ONLY_RELEVANT))

        currentStep(DELETING)
        if (revocationLock != null) {
            subFlow(DeleteRevocationLockFlow(revocationLock!!))
        }

        return finalizedTransaction
    }
}