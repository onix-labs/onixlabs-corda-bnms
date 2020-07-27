package io.onixlabs.corda.bnms.workflow.membership.centralized

import io.onixlabs.corda.bnms.contract.AttestationStatus
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import io.onixlabs.corda.bnms.workflow.getOutput
import io.onixlabs.corda.bnms.workflow.membership.IssueMembershipAttestationFlow
import io.onixlabs.corda.bnms.workflow.membership.IssueMembershipFlow
import io.onixlabs.corda.bnms.workflow.membership.RevokeMembershipAttestationFlow
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class RevokeMembershipAttestationFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction

    override fun initialize() {
        Pipeline
            .create(network)
            .run(memberNodeA) {
                val membership = Membership(centralizedNetwork, memberPartyA)
                IssueMembershipFlow.Initiator(membership)
            }
            .run(operatorNode) {
                val membership = it.getOutput<Membership>()
                val attestation = MembershipAttestation(operatorParty, membership, AttestationStatus.ACCEPTED)
                IssueMembershipAttestationFlow.Initiator(attestation)
            }
            .run(operatorNode) {
                val attestation = it.getOutput<MembershipAttestation>()
                RevokeMembershipAttestationFlow.Initiator(
                    attestation
                )
            }
            .finally { transaction = it }
    }

    @Test
    fun `RevokeMembershipAttestationFlow transaction should be signed by the initiator`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeMembershipAttestationFlow should record a transaction for all participants and observers`() {
        listOf(memberNodeA, operatorNode).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
                assertEquals(0, recordedTransaction.tx.outputs.size)
            }
        }
    }
}