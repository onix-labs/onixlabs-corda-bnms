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

class IssueRelationshipAttestationFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction
    private lateinit var attestation: RelationshipAttestation

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueRelationshipFlow.Initiator(RELATIONSHIP, checkMembership = false)
            }
            .run(nodeB) {
                val relationship = it.tx.outRefsOfType<Relationship>().single()
                attestation = relationship.accept(partyB)
                IssueRelationshipAttestationFlow.Initiator(attestation)
            }
            .finally { transaction = it }
    }

    @Test
    fun `IssueRelationshipAttestationFlow should be signed by the initiator`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `IssueRelationshipAttestationFlow should record a transaction for all relationship members`() {
        listOf(nodeA, nodeB).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }

    @Test
    fun `IssueRelationshipAttestationFlow should record a relationship attestation for all relationship members`() {
        listOf(nodeA, nodeB).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                val recordedAttestation = recordedTransaction.tx.outputsOfType<RelationshipAttestation>().singleOrNull()
                    ?: fail("Failed to find a recorded relationship attestation.")

                assertEquals(attestation, recordedAttestation)
            }
        }
    }
}