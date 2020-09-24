package io.onixlabs.corda.bnms.workflow.membership

import io.onixlabs.corda.bnms.contract.Role
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.getNextOutput
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class AmendMembershipFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction
    private lateinit var membership: Membership

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueMembershipFlow.Initiator(MEMBERSHIP, observers = setOf(partyB))
            }
            .run(nodeA) {
                val oldMembership = it.tx.outRefsOfType<Membership>().single()
                membership = oldMembership.getNextOutput().addRoles(Role.ADMINISTRATOR)
                AmendMembershipFlow.Initiator(oldMembership, membership, observers = setOf(partyB))
            }
            .finally { transaction = it }
    }

    @Test
    fun `AmendMembershipFlow transaction should be signed by the initiator`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendMembershipFlow should record a transaction for the membership holder and observers`() {
        listOf(nodeA, nodeB).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }

    @Test
    fun `AmendMembershipFlow should record a membership for the membership holder and observers`() {
        listOf(nodeA, nodeB).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                val recordedMembership = recordedTransaction.tx.outputsOfType<Membership>().singleOrNull()
                    ?: fail("Failed to find a recorded membership.")

                assertEquals(membership, recordedMembership)
                assert(recordedMembership.hasRole(Role.ADMINISTRATOR))
            }
        }
    }
}