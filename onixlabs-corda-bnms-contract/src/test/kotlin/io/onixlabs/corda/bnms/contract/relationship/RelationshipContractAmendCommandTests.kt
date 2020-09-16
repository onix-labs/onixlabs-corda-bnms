package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.bnms.contract.ContractTest
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RelationshipContractAmendCommandTests : ContractTest() {

    @Test
    fun `On relationship amending, the transaction must include the Amend command`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                input(issuedRelationship1.ref)
                output(RelationshipContract.ID, issuedRelationship1.getNextOutput())
                fails()
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship amending, only one relationship state must be consumed`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedRelationship2 = issue(RELATIONSHIP)
                input(issuedRelationship1.ref)
                input(issuedRelationship2.ref)
                output(RelationshipContract.ID, issuedRelationship1.getNextOutput())
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship amending, only one relationship state must be created`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                input(issuedRelationship1.ref)
                output(RelationshipContract.ID, issuedRelationship1.getNextOutput())
                output(RelationshipContract.ID, issuedRelationship1.getNextOutput())
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship amending, the network must not change`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                input(issuedRelationship1.ref)
                output(RelationshipContract.ID, issuedRelationship1.getNextOutput().copy(network = INVALID_NETWORK))
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_NETWORK)
            }
        }
    }

    @Test
    fun `On relationship amending, the previous state reference must be equal to the input state reference`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                input(issuedRelationship1.ref)
                output(RelationshipContract.ID, issuedRelationship1.getNextOutput().copy(previousStateRef = EMPTY_REF))
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_PREVIOUS_REF)
            }
        }
    }

    @Test
    fun `On relationship amending, all participants must sign the transaction`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                input(issuedRelationship1.ref)
                output(RelationshipContract.ID, issuedRelationship1.getNextOutput())
                command(keysOf(IDENTITY_C), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}