package io.onixlabs.corda.bnms.workflow.relationship

import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockStatus
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import io.onixlabs.corda.bnms.workflow.revocation.DeleteRevocationLockFlow
import io.onixlabs.corda.bnms.workflow.revocation.UpdateRevocationLockFlow
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class RevokeRelationshipFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction

    override fun initialize() {
        val issuanceTransaction = Pipeline
            .create(network)
            .run(nodeA) { IssueRelationshipFlow.Initiator(RELATIONSHIP, checkMembership = false) }
            .result

        Pipeline
            .create(network)
            .run(nodeA) {
                val lock = issuanceTransaction.tx
                    .outRefsOfType<RevocationLock<Relationship>>().single { it.state.data.owner == partyA }
                UpdateRevocationLockFlow(lock, RevocationLockStatus.UNLOCKED)
            }
            .run(nodeB) {
                val lock = issuanceTransaction.tx
                    .outRefsOfType<RevocationLock<Relationship>>().single { it.state.data.owner == partyB }
                DeleteRevocationLockFlow(lock)
            }
            .run(nodeB) {
                val relationship = issuanceTransaction.tx.outRefsOfType<Relationship>().single()
                RevokeRelationshipFlow.Initiator(relationship)
            }
            .finally { transaction = it }
    }

    @Test
    fun `AmendRelationshipFlow should be signed by the initiator`() {
        transaction.verifySignaturesExcept(partyA.owningKey)
    }

    @Test
    fun `AmendRelationshipFlow should be signed by all participants`() {
        transaction.verifySignaturesExcept(partyB.owningKey)
    }

    @Test
    fun `AmendRelationshipFlow should record a transaction for all relationship members`() {
        listOf(nodeA, nodeB).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }
}