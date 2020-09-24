package io.onixlabs.corda.bnms.contract.membership

import io.onixlabs.corda.bnms.contract.ContractTest
import io.onixlabs.corda.identity.framework.contract.EvolvableAttestationContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class MembershipAttestationContractIssueCommandTests : ContractTest() {

    @Test
    fun `On membership attestation issuance, the transaction must include the Issue command (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val attestation1 = issuedMembership1.accept(OPERATOR_A.party)
                output(MembershipAttestationContract.ID, attestation1)
                reference(issuedMembership1.ref)
                fails()
                command(keysOf(OPERATOR_A), EvolvableAttestationContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On membership attestation issuance, the transaction must include the Issue command (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val attestation1 = issuedMembership1.accept(IDENTITY_B.party)
                output(MembershipAttestationContract.ID, attestation1)
                reference(issuedMembership1.ref)
                fails()
                command(keysOf(IDENTITY_B), EvolvableAttestationContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On membership attestation issuing, only one membership state must be referenced (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val issuedMembership2 = issue(CENTRALIZED_MEMBERSHIP_B)
                val attestation1 = issuedMembership1.accept(OPERATOR_A.party)
                output(MembershipAttestationContract.ID, attestation1)
                reference(issuedMembership1.ref)
                reference(issuedMembership2.ref)
                command(keysOf(OPERATOR_A), EvolvableAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On membership attestation issuing, only one membership state must be referenced (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val issuedMembership2 = issue(DECENTRALIZED_MEMBERSHIP_B)
                val attestation1 = issuedMembership1.accept(IDENTITY_C.party)
                output(MembershipAttestationContract.ID, attestation1)
                reference(issuedMembership1.ref)
                reference(issuedMembership2.ref)
                command(keysOf(IDENTITY_C), EvolvableAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On membership attestation issuing, the attestation pointer must point to the referenced membership state (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val issuedMembership2 = issue(CENTRALIZED_MEMBERSHIP_B)
                val attestation1 = issuedMembership1.accept(OPERATOR_A.party)
                output(MembershipAttestationContract.ID, attestation1)
                reference(issuedMembership2.ref)
                command(keysOf(OPERATOR_A), EvolvableAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On membership attestation issuing, the attestation pointer must point to the referenced membership state (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val issuedMembership2 = issue(DECENTRALIZED_MEMBERSHIP_B)
                val attestation1 = issuedMembership1.accept(IDENTITY_C.party)
                output(MembershipAttestationContract.ID, attestation1)
                reference(issuedMembership2.ref)
                command(keysOf(IDENTITY_C), EvolvableAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On membership attestation issuing, the holder of the referenced membership state must be listed as an attestee (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val attestation1 = issuedMembership1.withWrongAttestee(OPERATOR_A.party, IDENTITY_B.party)
                output(MembershipAttestationContract.ID, attestation1)
                reference(issuedMembership1.ref)
                command(keysOf(OPERATOR_A), EvolvableAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_HOLDER_ATTESTEE)
            }
        }
    }

    @Test
    fun `On membership attestation issuing, the holder of the referenced membership state must be listed as an attestee (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val attestation1 = issuedMembership1.withWrongAttestee(IDENTITY_C.party, IDENTITY_B.party)
                output(MembershipAttestationContract.ID, attestation1)
                reference(issuedMembership1.ref)
                command(keysOf(IDENTITY_C), EvolvableAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_HOLDER_ATTESTEE)
            }
        }
    }

    @Test
    fun `On membership attestation issuing, the attestation network must be equal to the membership network (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val attestation1 = issuedMembership1.withWrongNetwork(OPERATOR_A.party, INVALID_NETWORK)
                output(MembershipAttestationContract.ID, attestation1)
                reference(issuedMembership1.ref)
                command(keysOf(OPERATOR_A), EvolvableAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_NETWORK)
            }
        }
    }

    @Test
    fun `On membership attestation issuing, the attestation network must be equal to the membership network (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val attestation1 = issuedMembership1.withWrongNetwork(IDENTITY_C.party, INVALID_NETWORK)
                output(MembershipAttestationContract.ID, attestation1)
                reference(issuedMembership1.ref)
                command(keysOf(IDENTITY_C), EvolvableAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_NETWORK)
            }
        }
    }

    @Test
    fun `On membership attestation issuing, if present, only the network operator must attest membership (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val attestation1 = issuedMembership1.accept(IDENTITY_C.party)
                output(MembershipAttestationContract.ID, attestation1)
                reference(issuedMembership1.ref)
                command(keysOf(IDENTITY_C), EvolvableAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_OPERATOR_ATTESTATION)
            }
        }
    }
}