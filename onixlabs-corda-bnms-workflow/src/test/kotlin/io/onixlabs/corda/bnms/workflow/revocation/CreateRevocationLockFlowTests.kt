package io.onixlabs.corda.bnms.workflow.revocation

import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import net.corda.core.transactions.SignedTransaction
import net.corda.testing.internal.vault.DummyLinearContract
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class CreateRevocationLockFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction
    private lateinit var lock: RevocationLock<DummyLinearContract.State>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                lock = REVOCATION_LOCK
                CreateRevocationLockFlow(lock)
            }
            .finally { transaction = it }
    }

    @Test
    fun `CreateRevocationLockFlow should be signed by the owner`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `CreateRevocationLockFlow should record a transaction for the owner`() {
        nodeA.transaction {
            val recordedTransaction = nodeA.services.validatedTransactions.getTransaction(transaction.id)
                ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

            assertEquals(transaction, recordedTransaction)
        }
    }

    @Test
    fun `CreateRevocationLockFlow should record a revocation lock for the owner`() {
        nodeA.transaction {
            val recordedTransaction = nodeA.services.validatedTransactions.getTransaction(transaction.id)
                ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

            val recordedRevocationLock = recordedTransaction.tx
                .outputsOfType<RevocationLock<DummyLinearContract.State>>().singleOrNull()
                ?: fail("Failed to find a recorded revocation lock.")

            assertEquals(lock, recordedRevocationLock)
        }
    }
}