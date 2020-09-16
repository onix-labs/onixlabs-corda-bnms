package io.onixlabs.corda.bnms.contract.membership

import io.onixlabs.corda.bnms.contract.ContractTest
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class MembershipContractAmendCommandTests : ContractTest() {

    @Test
    fun `On membership amending, the transaction must include the Amend command (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val amendedMembership1 = issuedMembership1.state.data.copy(previousStateRef = issuedMembership1.ref)
                input(issuedMembership1.ref)
                output(MembershipContract.ID, amendedMembership1)
                fails()
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On membership amending, the transaction must include the Amend command (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val amendedMembership1 = issuedMembership1.state.data.copy(previousStateRef = issuedMembership1.ref)
                input(issuedMembership1.ref)
                output(MembershipContract.ID, amendedMembership1)
                fails()
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On membership amending, only one membership state must be consumed (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val issuedMembership2 = issue(CENTRALIZED_MEMBERSHIP_A)
                val amendedMembership1 = issuedMembership1.state.data.copy(previousStateRef = issuedMembership1.ref)
                input(issuedMembership1.ref)
                input(issuedMembership2.ref)
                output(MembershipContract.ID, amendedMembership1)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership amending, only one membership state must be consumed (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val issuedMembership2 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val amendedMembership1 = issuedMembership1.state.data.copy(previousStateRef = issuedMembership1.ref)
                input(issuedMembership1.ref)
                input(issuedMembership2.ref)
                output(MembershipContract.ID, amendedMembership1)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership amending, only one membership state must be created (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val amendedMembership1 = issuedMembership1.state.data.copy(previousStateRef = issuedMembership1.ref)
                input(issuedMembership1.ref)
                output(MembershipContract.ID, amendedMembership1)
                output(MembershipContract.ID, amendedMembership1)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership amending, only one membership state must be created (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val amendedMembership1 = issuedMembership1.state.data.copy(previousStateRef = issuedMembership1.ref)
                input(issuedMembership1.ref)
                output(MembershipContract.ID, amendedMembership1)
                output(MembershipContract.ID, amendedMembership1)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership amending, the network of the membership must not change (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val amendedMembership1 = issuedMembership1.state.data.copy(
                    previousStateRef = issuedMembership1.ref,
                    network = INVALID_NETWORK
                )
                input(issuedMembership1.ref)
                output(MembershipContract.ID, amendedMembership1)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_NETWORK)
            }
        }
    }

    @Test
    fun `On membership amending, the network of the membership must not change (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val amendedMembership1 = issuedMembership1.state.data.copy(
                    previousStateRef = issuedMembership1.ref,
                    network = INVALID_NETWORK
                )
                input(issuedMembership1.ref)
                output(MembershipContract.ID, amendedMembership1)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_NETWORK)
            }
        }
    }

    @Test
    fun `On membership amending, the holder of the membership must not change (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val amendedMembership1 = issuedMembership1.state.data.copy(
                    previousStateRef = issuedMembership1.ref,
                    holder = IDENTITY_B.party
                )
                input(issuedMembership1.ref)
                output(MembershipContract.ID, amendedMembership1)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_HOLDER)
            }
        }
    }

    @Test
    fun `On membership amending, the holder of the membership must not change (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val amendedMembership1 = issuedMembership1.state.data.copy(
                    previousStateRef = issuedMembership1.ref,
                    holder = IDENTITY_B.party
                )
                input(issuedMembership1.ref)
                output(MembershipContract.ID, amendedMembership1)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_HOLDER)
            }
        }
    }

    @Test
    fun `On membership amending, a network operator must possess the network operator role (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_OPERATOR)
                val amendedMembership1 = issuedMembership1.state.data.copy(
                    previousStateRef = issuedMembership1.ref,
                    roles = emptySet()
                )
                input(issuedMembership1.ref)
                output(MembershipContract.ID, amendedMembership1)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_ROLE)
            }
        }
    }

    @Test
    fun `On membership amending, the previous state reference of the created membership state must be equal to the state reference of the consumed membership state (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val amendedMembership1 = issuedMembership1.state.data
                input(issuedMembership1.ref)
                output(MembershipContract.ID, amendedMembership1)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_PREVIOUS_STATE_REF)
            }
        }
    }

    @Test
    fun `On membership amending, the previous state reference of the created membership state must be equal to the state reference of the consumed membership state (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val amendedMembership1 = issuedMembership1.state.data
                input(issuedMembership1.ref)
                output(MembershipContract.ID, amendedMembership1)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_PREVIOUS_STATE_REF)
            }
        }
    }

    @Test
    fun `On membership amending, either the holder or the network operator of the created membership state must sign the transaction (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val amendedMembership1 = issuedMembership1.state.data.copy(previousStateRef = issuedMembership1.ref)
                input(issuedMembership1.ref)
                output(MembershipContract.ID, amendedMembership1)
                command(keysOf(IDENTITY_B), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On membership amending, either the holder or the network operator of the created membership state must sign the transaction (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val amendedMembership1 = issuedMembership1.state.data.copy(previousStateRef = issuedMembership1.ref)
                input(issuedMembership1.ref)
                output(MembershipContract.ID, amendedMembership1)
                command(keysOf(IDENTITY_B), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}