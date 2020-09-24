package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.bnms.contract.ContractTest
import io.onixlabs.corda.identity.framework.contract.EvolvableAttestationContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RelationshipAttestationContractIssueCommandTests : ContractTest() {

    @Test
    fun `On relationship attestation issuing, the transaction must include the Issue command`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val attestation1 = issuedRelationship1.accept(IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation1)
                reference(issuedRelationship1.ref)
                fails()
                command(keysOf(IDENTITY_A, IDENTITY_B), EvolvableAttestationContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship attestation issuing, only one relationship state must be referenced`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedRelationship2 = issue(RELATIONSHIP)
                val attestation1 = issuedRelationship1.accept(IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation1)
                reference(issuedRelationship1.ref)
                reference(issuedRelationship2.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B), EvolvableAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On relationship attestation issuing, the attestation pointer must point to the referenced membership state`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val issuedRelationship2 = issue(RELATIONSHIP)
                val attestation1 = issuedRelationship1.accept(IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation1)
                reference(issuedRelationship2.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B), EvolvableAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On relationship attestation issuing, the attestor must be a participant of the referenced relationship state`() {
        services.ledger {
            transaction {
                val issuedRelationship1 = issue(RELATIONSHIP)
                val attestation1 = issuedRelationship1.accept(IDENTITY_C.party)
                output(RelationshipAttestationContract.ID, attestation1)
                reference(issuedRelationship1.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), EvolvableAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_ATTESTOR)
            }
        }
    }
}