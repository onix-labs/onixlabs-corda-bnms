package io.onixlabs.corda.bnms.workflow.relationship

import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class IssueRelationshipFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction
    private lateinit var relationship: Relationship

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                relationship = RELATIONSHIP
                IssueRelationshipFlow.Initiator(relationship, checkMembership = false)
            }
            .finally { transaction = it }
    }

    @Test
    fun `IssueRelationshipFlow should be signed by the initiator`() {
        transaction.verifySignaturesExcept(partyB.owningKey)
    }

    @Test
    fun `IssueRelationshipFlow should be signed by all participants`() {
        transaction.verifySignaturesExcept(partyA.owningKey)
    }

    @Test
    fun `IssueRelationshipFlow should record a transaction for all relationship members`() {
        listOf(nodeA, nodeB).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }

    @Test
    fun `IssueRelationshipFlow should record a relationship for all relationship members`() {
        listOf(nodeA, nodeB).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                val recordedRelationship = recordedTransaction.tx.outputsOfType<Relationship>().singleOrNull()
                    ?: fail("Failed to find a recorded relationship.")

                assertEquals(relationship, recordedRelationship)
            }
        }
    }
}