package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.identity.framework.contract.EvolvableAttestationContract
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractClassName
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class RelationshipAttestationContract : EvolvableAttestationContract(), Contract {

    companion object {
        @JvmStatic
        val ID: ContractClassName = this::class.java.enclosingClass.canonicalName
    }

    internal object Issue {
        const val CONTRACT_RULE_REFERENCES =
            "On relationship attestation issuing, only one relationship state must be referenced."

        const val CONTRACT_RULE_POINTER =
            "On relationship attestation issuing, the attestation pointer must point to the referenced membership state."

        const val CONTRACT_RULE_NETWORK =
            "On relationship attestation issuing, the attestation network must be equal to the relationship network."

        const val CONTRACT_RULE_ATTESTOR =
            "On relationship attestation issuing, the attestor must be a participant of the referenced relationship state."
    }

    internal object Amend {
        const val CONTRACT_RULE_REFERENCES =
            "On relationship attestation amending, only one relationship state must be referenced."

        const val CONTRACT_RULE_POINTER =
            "On relationship attestation amending, the attestation pointer must point to the referenced membership state."

        const val CONTRACT_RULE_NETWORK =
            "On relationship attestation amending, the attestation network must be equal to the relationship network."

        const val CONTRACT_RULE_ATTESTOR =
            "On relationship attestation amending, the attestor must be a participant of the referenced relationship state."
    }

    override fun onVerifyIssue(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val references = transaction.referenceInputRefsOfType<Relationship>()
        val attestation = transaction.outputsOfType<RelationshipAttestation>().single()

        Issue.CONTRACT_RULE_REFERENCES using (references.size == 1)

        val relationshipRef = references.single().ref
        val relationship = references.single().state.data

        Issue.CONTRACT_RULE_POINTER using (attestation.pointer.pointer == relationshipRef)
        Issue.CONTRACT_RULE_NETWORK using (attestation.network == relationship.network)
        Issue.CONTRACT_RULE_ATTESTOR using (attestation.attestor in relationship.participants)
    }

    override fun onVerifyAmend(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val references = transaction.referenceInputRefsOfType<Relationship>()
        val attestation = transaction.outputsOfType<RelationshipAttestation>().single()

        Amend.CONTRACT_RULE_REFERENCES using (references.size == 1)

        val relationshipRef = references.single().ref
        val relationship = references.single().state.data

        Amend.CONTRACT_RULE_POINTER using (attestation.pointer.pointer == relationshipRef)
        Amend.CONTRACT_RULE_NETWORK using (attestation.network == relationship.network)
        Amend.CONTRACT_RULE_ATTESTOR using (attestation.attestor in relationship.participants)
    }
}