package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.bnms.contract.ContractTest
import io.onixlabs.corda.identity.framework.contract.EvolvableAttestationContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RelationshipAttestationContractAmendCommandTests : ContractTest() {

    @Test
    fun `On relationship attestation amending, the transaction must include the Amend command`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedAttestation1 = issue(issuedRelationship1.accept(IDENTITY_A.party), issuedRelationship1)
                val amendedAttestation1 = issuedAttestation1.reject()
                input(issuedAttestation1.ref)
                output(RelationshipAttestationContract.ID, amendedAttestation1)
                reference(issuedRelationship1.ref)
                fails()
                command(keysOf(IDENTITY_A, IDENTITY_B), EvolvableAttestationContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship attestation amending, only one relationship state must be referenced`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedRelationship2 = issue(RELATIONSHIP)
                val issuedAttestation1 = issue(issuedRelationship1.accept(IDENTITY_A.party), issuedRelationship1)
                val amendedAttestation1 = issuedAttestation1.reject()
                input(issuedAttestation1.ref)
                output(RelationshipAttestationContract.ID, amendedAttestation1)
                reference(issuedRelationship1.ref)
                reference(issuedRelationship2.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B), EvolvableAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On relationship attestation amending, the attestation pointer must point to the referenced membership state`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedRelationship2 = issue(RELATIONSHIP)
                val issuedAttestation1 = issue(issuedRelationship1.accept(IDENTITY_A.party), issuedRelationship1)
                val amendedAttestation1 = issuedAttestation1.reject()
                input(issuedAttestation1.ref)
                output(RelationshipAttestationContract.ID, amendedAttestation1)
                reference(issuedRelationship2.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B), EvolvableAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_POINTER)
            }
        }
    }
}