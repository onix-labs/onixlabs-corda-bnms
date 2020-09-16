package io.onixlabs.corda.bnms.contract.membership

import io.onixlabs.corda.bnms.contract.ContractTest
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class MembershipContractIssueCommandTests : ContractTest() {

    @Test
    fun `On membership issuing, the transaction must include the Issue command (centralized)`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                fails()
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On membership issuing, the transaction must include the Issue command (decentralized)`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                fails()
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On membership issuing, zero membership states must be consumed (centralized)`() {
        services.ledger {
            transaction {
                input(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership issuing, zero membership states must be consumed (decentralized)`() {
        services.ledger {
            transaction {
                input(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                output(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership issuing, only one membership state must be created (centralized)`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership issuing, only one membership state must be created (decentralized)`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                output(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership issuing, a network operator must possess the network operator role (centralized)`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_OPERATOR.copy(roles = emptySet()))
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_ROLE)
            }
        }
    }

    @Test
    fun `On membership issuing, the previous state reference of the created membership state must be null (centralized)`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A.copy(previousStateRef = EMPTY_REF))
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_PREVIOUS_STATE_REF)
            }
        }
    }

    @Test
    fun `On membership issuing, the previous state reference of the created membership state must be null (decentralized)`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A.copy(previousStateRef = EMPTY_REF))
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_PREVIOUS_STATE_REF)
            }
        }
    }

    @Test
    fun `On membership issuing, the holder of the created membership state must sign the transaction (centralized)`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                command(keysOf(OPERATOR_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On membership issuing, the holder of the created membership state must sign the transaction (decentralized)`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                command(keysOf(OPERATOR_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}