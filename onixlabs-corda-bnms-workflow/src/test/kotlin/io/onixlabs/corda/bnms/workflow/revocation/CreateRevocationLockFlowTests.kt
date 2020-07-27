package io.onixlabs.corda.bnms.workflow.revocation

import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.workflow.DummyLinearState
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import io.onixlabs.corda.bnms.workflow.getOutput
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class CreateRevocationLockFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction
    private lateinit var revocationLock: RevocationLock<DummyLinearState>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(memberNodeA) {
                val revocationLock = RevocationLock.create(memberPartyA, DummyLinearState())
                CreateRevocationLockFlow(revocationLock)
            }
            .finally {
                transaction = it
                revocationLock = it.getOutput<RevocationLock<DummyLinearState>>().state.data
            }
    }

    @Test
    fun `CreateRevocationLockFlow transaction should be signed by the owner`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `CreateRevocationLockFlow should record a transaction for the owner`() {
        memberNodeA.transaction {
            val recordedTransaction = memberNodeA.services.validatedTransactions.getTransaction(transaction.id)
                ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

            assertEquals(transaction, recordedTransaction)
            assertEquals(0, recordedTransaction.tx.inputs.size)
            assertEquals(1, recordedTransaction.tx.outputs.size)
        }
    }

    @Test
    fun `CreateRevocationLockFlow should record a revocation lock state for the owner`() {
        memberNodeA.transaction {
            val recordedTransaction = memberNodeA.services.validatedTransactions.getTransaction(transaction.id)
                ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

            val recordedRevocationLock = recordedTransaction
                .tx.outputsOfType<RevocationLock<DummyLinearState>>().single()

            assertEquals(revocationLock, recordedRevocationLock)
        }
    }
}