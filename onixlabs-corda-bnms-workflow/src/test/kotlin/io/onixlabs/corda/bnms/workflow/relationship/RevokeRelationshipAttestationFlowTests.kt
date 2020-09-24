package io.onixlabs.corda.bnms.workflow.relationship

import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.contract.relationship.accept
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class RevokeRelationshipAttestationFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueRelationshipFlow.Initiator(RELATIONSHIP, checkMembership = false)
            }
            .run(nodeB) {
                val relationship = it.tx.outRefsOfType<Relationship>().single()
                val attestation = relationship.accept(partyB)
                IssueRelationshipAttestationFlow.Initiator(attestation)
            }
            .run(nodeB) {
                val attestation = it.tx.outRefsOfType<RelationshipAttestation>().single()
                RevokeRelationshipAttestationFlow.Initiator(attestation)
            }
            .finally { transaction = it }
    }

    @Test
    fun `RevokeRelationshipAttestationFlow should be signed by the initiator`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeRelationshipAttestationFlow should record a transaction for all relationship members`() {
        listOf(nodeA, nodeB).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }
}