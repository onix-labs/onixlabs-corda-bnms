package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.bnms.contract.ContractTest
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RelationshipContractRevokeCommandTests : ContractTest() {

    @Test
    fun `On relationship revoking, the transaction must include the Revoke command`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                input(issuedRelationship1.ref)
                fails()
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship revoking, only one relationship state must be consumed`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedRelationship2 = issue(RELATIONSHIP)
                input(issuedRelationship1.ref)
                input(issuedRelationship2.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship revoking, zero relationship states must be created`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                input(issuedRelationship1.ref)
                output(RelationshipContract.ID, issuedRelationship1.getNextOutput())
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship revoking, all participants must sign the transaction`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                input(issuedRelationship1.ref)
                command(keysOf(IDENTITY_A), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}