package io.onixlabs.corda.bnms.contract.membership

import io.onixlabs.corda.bnms.contract.ContractTest
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class MembershipContractRevokeCommandTests : ContractTest() {

    @Test
    fun `On membership revoking, the transaction must include the Revoke command (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                input(issuedMembership1.ref)
                fails()
                command(keysOf(IDENTITY_A), MembershipContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On membership issuing, the transaction must include the Amend command (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                input(issuedMembership1.ref)
                fails()
                command(keysOf(IDENTITY_A), MembershipContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On membership revoking, only one membership state must be consumed (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val issuedMembership2 = issue(CENTRALIZED_MEMBERSHIP_A)
                input(issuedMembership1.ref)
                input(issuedMembership2.ref)
                command(keysOf(IDENTITY_A), MembershipContract.Revoke)
                failsWith(MembershipContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership revoking, only one membership state must be consumed (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val issuedMembership2 = issue(DECENTRALIZED_MEMBERSHIP_A)
                input(issuedMembership1.ref)
                input(issuedMembership2.ref)
                command(keysOf(IDENTITY_A), MembershipContract.Revoke)
                failsWith(MembershipContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership revoking, zero membership states must be created (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val amendedMembership1 = issuedMembership1.state.data.copy(previousStateRef = issuedMembership1.ref)
                input(issuedMembership1.ref)
                output(MembershipContract.ID, amendedMembership1)
                command(keysOf(IDENTITY_A), MembershipContract.Revoke)
                failsWith(MembershipContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership revoking, zero membership states must be created (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val amendedMembership1 = issuedMembership1.state.data.copy(previousStateRef = issuedMembership1.ref)
                input(issuedMembership1.ref)
                output(MembershipContract.ID, amendedMembership1)
                command(keysOf(IDENTITY_A), MembershipContract.Revoke)
                failsWith(MembershipContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership revoking, either the holder or the network operator of the consumed membership state must sign the transaction (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                input(issuedMembership1.ref)
                command(keysOf(IDENTITY_B), MembershipContract.Revoke)
                failsWith(MembershipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On membership revoking, either the holder or the network operator of the consumed membership state must sign the transaction (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                input(issuedMembership1.ref)
                command(keysOf(IDENTITY_B), MembershipContract.Revoke)
                failsWith(MembershipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}