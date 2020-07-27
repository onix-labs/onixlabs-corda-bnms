package io.onixlabs.corda.bnms.workflow.membership.centralized

import io.onixlabs.corda.bnms.contract.Role
import io.onixlabs.corda.bnms.contract.getNextOutput
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import io.onixlabs.corda.bnms.workflow.getOutput
import io.onixlabs.corda.bnms.workflow.membership.AmendMembershipFlow
import io.onixlabs.corda.bnms.workflow.membership.IssueMembershipFlow
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
            .run(memberNodeA) {
                val membership = Membership(centralizedNetwork, memberPartyA)
                IssueMembershipFlow.Initiator(membership)
            }
            .run(memberNodeA) {
                val oldMembership = it.getOutput<Membership>()
                val newMembership = oldMembership.getNextOutput().addRoles(Role.USER)
                AmendMembershipFlow.Initiator(oldMembership, newMembership)
            }
            .run(operatorNode) {
                val oldMembership = it.getOutput<Membership>()
                val newMembership = oldMembership.getNextOutput().addRoles(Role.ADMINISTRATOR)
                AmendMembershipFlow.Initiator(oldMembership, newMembership)
            }
            .finally {
                transaction = it
                membership = it.getOutput<Membership>().state.data
            }
    }

    @Test
    fun `AmendMembershipFlow transaction should be signed by the initiator`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendMembershipFlow should record a transaction for all participants and observers`() {
        listOf(memberNodeA, operatorNode).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
                assertEquals(1, recordedTransaction.tx.outputs.size)
            }
        }
    }

    @Test
    fun `AmendMembershipFlow should record a membership state for all participants and observers`() {
        listOf(memberNodeA, operatorNode).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                val recordedMembership = recordedTransaction
                    .tx.outputsOfType<Membership>().single()

                assertEquals(membership, recordedMembership)
                assert(recordedMembership.hasRole(Role.USER))
                assert(recordedMembership.hasRole(Role.ADMINISTRATOR))
            }
        }
    }
}