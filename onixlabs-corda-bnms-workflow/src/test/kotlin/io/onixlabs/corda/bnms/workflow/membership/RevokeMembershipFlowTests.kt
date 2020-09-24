package io.onixlabs.corda.bnms.workflow.membership

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class RevokeMembershipFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueMembershipFlow.Initiator(MEMBERSHIP, observers = setOf(partyB))
            }
            .run(nodeA) {
                val membership = it.tx.outRefsOfType<Membership>().single()
                RevokeMembershipFlow.Initiator(membership, observers = setOf(partyB))
            }
            .finally { transaction = it }
    }

    @Test
    fun `RevokeMembershipFlow transaction should be signed by the initiator`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeMembershipFlow should record a transaction for the membership holder and observers`() {
        listOf(nodeA, nodeB).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }
}