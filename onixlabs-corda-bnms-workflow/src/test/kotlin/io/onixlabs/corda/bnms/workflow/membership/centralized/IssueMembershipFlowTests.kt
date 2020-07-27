package io.onixlabs.corda.bnms.workflow.membership.centralized

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import io.onixlabs.corda.bnms.workflow.getOutput
import io.onixlabs.corda.bnms.workflow.membership.IssueMembershipFlow
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class IssueMembershipFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction
    private lateinit var membership: Membership

    override fun initialize() {
        Pipeline
            .create(network)
            .run(memberNodeA) {
                val membership = Membership(centralizedNetwork, memberPartyA)
                IssueMembershipFlow.Initiator(membership)
            }
            .finally {
                transaction = it
                membership = it.getOutput<Membership>().state.data
            }
    }

    @Test
    fun `IssueMembershipFlow transaction should be signed by the initiator`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `IssueMembershipFlow should record a transaction for all participants and observers`() {
        listOf(memberNodeA, operatorNode).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(0, recordedTransaction.tx.inputs.size)
                assertEquals(1, recordedTransaction.tx.outputs.size)
            }
        }
    }

    @Test
    fun `IssueMembershipFlow should record a membership state for all participants and observers`() {
        listOf(memberNodeA, operatorNode).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                val recordedMembership = recordedTransaction
                    .tx.outputsOfType<Membership>().single()

                assertEquals(membership, recordedMembership)
            }
        }
    }
}