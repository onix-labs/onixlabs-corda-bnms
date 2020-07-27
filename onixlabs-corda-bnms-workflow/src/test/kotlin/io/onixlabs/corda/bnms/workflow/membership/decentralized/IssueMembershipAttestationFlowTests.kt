package io.onixlabs.corda.bnms.workflow.membership.decentralized

import io.onixlabs.corda.bnms.contract.AttestationStatus
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import io.onixlabs.corda.bnms.workflow.getOutput
import io.onixlabs.corda.bnms.workflow.membership.IssueMembershipAttestationFlow
import io.onixlabs.corda.bnms.workflow.membership.IssueMembershipFlow
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class IssueMembershipAttestationFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction
    private lateinit var membershipAttestation: MembershipAttestation

    override fun initialize() {
        Pipeline
            .create(network)
            .run(memberNodeA) {
                val membership = Membership(decentralizedNetwork, memberPartyA)
                IssueMembershipFlow.Initiator(
                    membership,
                    observers = setOf(memberPartyB, memberPartyC)
                )
            }
            .run(memberNodeB) {
                val membership = it.getOutput<Membership>()
                val attestation = MembershipAttestation(memberPartyB, membership, AttestationStatus.ACCEPTED)
                IssueMembershipAttestationFlow.Initiator(
                    attestation,
                    observers = setOf(memberPartyC)
                )
            }
            .finally {
                transaction = it
                membershipAttestation = it.getOutput<MembershipAttestation>().state.data
            }
    }

    @Test
    fun `IssueMembershipAttestationFlow transaction should be signed by the initiator`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `IssueMembershipAttestationFlow should record a transaction for all participants and observers`() {
        listOf(memberNodeA, memberNodeB, memberNodeC).forEach {
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
    fun `IssueMembershipAttestationFlow should record a membership state for all participants and observers`() {
        listOf(memberNodeA, memberNodeB, memberNodeC).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                val recordedMembershipAttestation = recordedTransaction
                    .tx.outputsOfType<MembershipAttestation>().single()

                assertEquals(membershipAttestation, recordedMembershipAttestation)
            }
        }
    }
}