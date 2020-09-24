package io.onixlabs.corda.bnms.workflow.revocation

import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import net.corda.core.transactions.SignedTransaction
import net.corda.testing.internal.vault.DummyLinearContract
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class DeleteRevocationLockFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                CreateRevocationLockFlow(REVOCATION_LOCK)
            }
            .run(nodeA) {
                val lock = it.tx.outRefsOfType<RevocationLock<DummyLinearContract.State>>().single()
                DeleteRevocationLockFlow(lock)
            }
            .finally { transaction = it }
    }

    @Test
    fun `DeleteRevocationLockFlow should be signed by the owner`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `DeleteRevocationLockFlow should record a transaction for the owner`() {
        nodeA.transaction {
            val recordedTransaction = nodeA.services.validatedTransactions.getTransaction(transaction.id)
                ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

            assertEquals(transaction, recordedTransaction)
        }
    }
}