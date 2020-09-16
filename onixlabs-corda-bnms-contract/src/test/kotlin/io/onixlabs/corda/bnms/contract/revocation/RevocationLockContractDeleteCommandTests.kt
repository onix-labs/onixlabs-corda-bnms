package io.onixlabs.corda.bnms.contract.revocation

import io.onixlabs.corda.bnms.contract.*
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RevocationLockContractDeleteCommandTests : ContractTest() {

    @Test
    fun `On revocation lock deleting, the transaction must include the Create command`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                fails()
                command(keysOf(IDENTITY_A), RevocationLockContract.Delete)
                verifies()
            }
        }
    }

    @Test
    fun `On revocation lock deleting, only one state must be consumed`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                command(keysOf(IDENTITY_A), RevocationLockContract.Delete)
                failsWith(RevocationLockContract.Delete.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On revocation lock deleting, zero states must be created`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                output(RevocationLockContract.ID, REVOCATION_LOCK)
                command(keysOf(IDENTITY_A), RevocationLockContract.Delete)
                failsWith(RevocationLockContract.Delete.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On revocation lock deleting, only the owner must sign the transaction`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                command(keysOf(IDENTITY_B), RevocationLockContract.Delete)
                failsWith(RevocationLockContract.Delete.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}