package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.bnms.contract.ContractTest
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RelationshipContractIssueCommandTests : ContractTest() {

    @Test
    fun `On relationship issuing, the transaction must include the Issue command`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, RELATIONSHIP)
                RELATIONSHIP.createRevocationLocks().forEach {
                    output(RevocationLockContract.ID, it)
                }
                command(keysOf(IDENTITY_A, IDENTITY_B), RevocationLockContract.Create)
                fails()
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship issuing, zero relationship states must be consumed`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, RELATIONSHIP)
                output(RelationshipContract.ID, RELATIONSHIP)
                RELATIONSHIP.createRevocationLocks().forEach { output(RevocationLockContract.ID, it) }
                command(keysOf(IDENTITY_A, IDENTITY_B), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship issuing, only one relationship state must be created`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, RELATIONSHIP)
                output(RelationshipContract.ID, RELATIONSHIP)
                RELATIONSHIP.createRevocationLocks().forEach { output(RevocationLockContract.ID, it) }
                command(keysOf(IDENTITY_A, IDENTITY_B), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship issuing, revocation locks must be issued for all relationship members`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, RELATIONSHIP)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_LOCKS)
            }
        }
    }

    @Test
    fun `On relationship issuing, all revocation locks must point to the relationship state`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, RELATIONSHIP)
                INVALID_RELATIONSHIP.createRevocationLocks().forEach { output(RevocationLockContract.ID, it) }
                command(keysOf(IDENTITY_A, IDENTITY_B), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_REVOCATION_LOCK_POINTERS)
            }
        }
    }

    @Test
    fun `On relationship issuing, the previous state reference of the created relationship state must be null`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, RELATIONSHIP.copy(previousStateRef = EMPTY_REF))
                RELATIONSHIP.createRevocationLocks().forEach { output(RevocationLockContract.ID, it) }
                command(keysOf(IDENTITY_A, IDENTITY_B), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_PREVIOUS_REF)
            }
        }
    }

    @Test
    fun `On relationship issuing, all participants must sign the transaction`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, RELATIONSHIP)
                RELATIONSHIP.createRevocationLocks().forEach { output(RevocationLockContract.ID, it) }
                command(keysOf(IDENTITY_A, IDENTITY_B), RevocationLockContract.Create)
                command(keysOf(IDENTITY_C), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}