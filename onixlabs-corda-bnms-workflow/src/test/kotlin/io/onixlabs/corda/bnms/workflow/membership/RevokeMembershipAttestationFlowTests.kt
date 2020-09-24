package io.onixlabs.corda.bnms.workflow.membership

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.accept
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class RevokeMembershipAttestationFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueMembershipFlow.Initiator(MEMBERSHIP, observers = setOf(partyB))
            }
            .run(nodeB) {
                val membership = it.tx.outRefsOfType<Membership>().single()
                val attestation = membership.accept(partyB)
                IssueMembershipAttestationFlow.Initiator(attestation)
            }
            .run(nodeB) {
                val attestation = it.tx.outRefsOfType<MembershipAttestation>().single()
                RevokeMembershipAttestationFlow.Initiator(attestation)
            }
            .finally { transaction = it }
    }

    @Test
    fun `RevokeMembershipAttestationFlow transaction should be signed by the initiator`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeMembershipAttestationFlow should record a transaction for the attestor and membership holder`() {
        listOf(nodeA, nodeB).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }
}