package io.onixlabs.corda.bnms.contract.membership

import io.onixlabs.corda.bnms.contract.ContractTest
import io.onixlabs.corda.identity.framework.contract.EvolvableAttestationContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class MembershipAttestationContractAmendCommandTests : ContractTest() {

    @Test
    fun `On membership attestation amending, the transaction must include the Amend command (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val issuedAttestation1 = issue(issuedMembership1.accept(OPERATOR_A.party), issuedMembership1)
                val amendedAttestation1 = issuedAttestation1.reject()
                input(issuedAttestation1.ref)
                output(MembershipAttestationContract.ID, amendedAttestation1)
                reference(issuedMembership1.ref)
                fails()
                command(keysOf(OPERATOR_A), EvolvableAttestationContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On membership attestation amending, the transaction must include the Amend command (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val issuedAttestation1 = issue(issuedMembership1.accept(IDENTITY_C.party), issuedMembership1)
                val amendedAttestation1 = issuedAttestation1.reject()
                input(issuedAttestation1.ref)
                output(MembershipAttestationContract.ID, amendedAttestation1)
                reference(issuedMembership1.ref)
                fails()
                command(keysOf(IDENTITY_C), EvolvableAttestationContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On membership attestation amending, only one membership state must be referenced (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val issuedMembership2 = issue(CENTRALIZED_MEMBERSHIP_B)
                val issuedAttestation1 = issue(issuedMembership1.accept(OPERATOR_A.party), issuedMembership1)
                val amendedAttestation1 = issuedAttestation1.reject()
                input(issuedAttestation1.ref)
                output(MembershipAttestationContract.ID, amendedAttestation1)
                reference(issuedMembership1.ref)
                reference(issuedMembership2.ref)
                command(keysOf(OPERATOR_A), EvolvableAttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On membership attestation amending, only one membership state must be referenced (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val issuedMembership2 = issue(DECENTRALIZED_MEMBERSHIP_B)
                val issuedAttestation1 = issue(issuedMembership1.accept(IDENTITY_C.party), issuedMembership1)
                val amendedAttestation1 = issuedAttestation1.reject()
                input(issuedAttestation1.ref)
                output(MembershipAttestationContract.ID, amendedAttestation1)
                reference(issuedMembership1.ref)
                reference(issuedMembership2.ref)
                command(keysOf(IDENTITY_C), EvolvableAttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On membership attestation amending, the attestation pointer must point to the referenced membership state (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val issuedMembership2 = issue(CENTRALIZED_MEMBERSHIP_B)
                val issuedAttestation1 = issue(issuedMembership1.accept(OPERATOR_A.party), issuedMembership1)
                val amendedAttestation1 = issuedAttestation1.reject()
                input(issuedAttestation1.ref)
                output(MembershipAttestationContract.ID, amendedAttestation1)
                reference(issuedMembership2.ref)
                command(keysOf(OPERATOR_A), EvolvableAttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On membership attestation amending, the attestation pointer must point to the referenced membership state (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val issuedMembership2 = issue(DECENTRALIZED_MEMBERSHIP_B)
                val issuedAttestation1 = issue(issuedMembership1.accept(IDENTITY_C.party), issuedMembership1)
                val amendedAttestation1 = issuedAttestation1.reject()
                input(issuedAttestation1.ref)
                output(MembershipAttestationContract.ID, amendedAttestation1)
                reference(issuedMembership2.ref)
                command(keysOf(IDENTITY_C), EvolvableAttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On membership attestation amending, the holder of the referenced membership state must be listed as an attestee (centralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(CENTRALIZED_MEMBERSHIP_A)
                val issuedAttestation1 = issue(issuedMembership1.accept(OPERATOR_A.party), issuedMembership1)
                val amendedAttestation1 = issuedMembership1.withWrongAttestee(issuedAttestation1, IDENTITY_B.party)
                input(issuedAttestation1.ref)
                output(MembershipAttestationContract.ID, amendedAttestation1)
                reference(issuedMembership1.ref)
                command(keysOf(OPERATOR_A), EvolvableAttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_HOLDER_ATTESTEE)
            }
        }
    }

    @Test
    fun `On membership attestation amending, the holder of the referenced membership state must be listed as an attestee (decentralized)`() {
        services.ledger {
            transaction {
                val issuedMembership1 = issue(DECENTRALIZED_MEMBERSHIP_A)
                val issuedAttestation1 = issue(issuedMembership1.accept(IDENTITY_C.party), issuedMembership1)
                val amendedAttestation1 = issuedMembership1.withWrongAttestee(issuedAttestation1, IDENTITY_B.party)
                input(issuedAttestation1.ref)
                output(MembershipAttestationContract.ID, amendedAttestation1)
                reference(issuedMembership1.ref)
                command(keysOf(IDENTITY_C), EvolvableAttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_HOLDER_ATTESTEE)
            }
        }
    }
}