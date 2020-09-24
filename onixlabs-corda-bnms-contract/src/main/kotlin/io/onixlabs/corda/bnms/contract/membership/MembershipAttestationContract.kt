package io.onixlabs.corda.bnms.contract.membership

import io.onixlabs.corda.identity.framework.contract.EvolvableAttestationContract
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractClassName
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class MembershipAttestationContract : EvolvableAttestationContract(), Contract {

    companion object {
        @JvmStatic
        val ID: ContractClassName = this::class.java.enclosingClass.canonicalName
    }

    internal object Issue {
        const val CONTRACT_RULE_REFERENCES =
            "On membership attestation issuing, only one membership state must be referenced."

        const val CONTRACT_RULE_POINTER =
            "On membership attestation issuing, the attestation pointer must point to the referenced membership state."

        const val CONTRACT_RULE_HOLDER_ATTESTEE =
            "On membership attestation issuing, the holder of the referenced membership state must be listed as an attestee."

        const val CONTRACT_RULE_NETWORK =
            "On membership attestation issuing, the attestation network must be equal to the membership network."

        const val CONTRACT_RULE_OPERATOR_ATTESTATION =
            "On membership attestation issuing, if present, only the network operator must attest membership."
    }

    internal object Amend {
        const val CONTRACT_RULE_REFERENCES =
            "On membership attestation amending, only one membership state must be referenced."

        const val CONTRACT_RULE_POINTER =
            "On membership attestation amending, the attestation pointer must point to the referenced membership state."

        const val CONTRACT_RULE_HOLDER_ATTESTEE =
            "On membership attestation amending, the holder of the referenced membership state must be listed as an attestee."

        const val CONTRACT_RULE_NETWORK =
            "On membership attestation amending, the attestation network must be equal to the membership network."

        const val CONTRACT_RULE_OPERATOR_ATTESTATION =
            "On membership attestation amending, if present, only the network operator must attest membership."
    }

    override fun onVerifyIssue(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val references = transaction.referenceInputRefsOfType<Membership>()
        val attestation = transaction.outputsOfType<MembershipAttestation>().single()

        Issue.CONTRACT_RULE_REFERENCES using (references.size == 1)

        val membershipRef = references.single().ref
        val membership = references.single().state.data

        Issue.CONTRACT_RULE_POINTER using (attestation.pointer.pointer == membershipRef)
        Issue.CONTRACT_RULE_HOLDER_ATTESTEE using (membership.holder in attestation.attestees)
        Issue.CONTRACT_RULE_NETWORK using (attestation.network == membership.network)

        if (membership.network.operator != null) {
            Issue.CONTRACT_RULE_OPERATOR_ATTESTATION using (attestation.attestor == membership.network.operator)
        }
    }

    override fun onVerifyAmend(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val references = transaction.referenceInputRefsOfType<Membership>()
        val attestation = transaction.outputsOfType<MembershipAttestation>().single()

        Amend.CONTRACT_RULE_REFERENCES using (references.size == 1)

        val membershipRef = references.single().ref
        val membership = references.single().state.data

        Amend.CONTRACT_RULE_POINTER using (attestation.pointer.pointer == membershipRef)
        Amend.CONTRACT_RULE_HOLDER_ATTESTEE using (membership.holder in attestation.attestees)
        Amend.CONTRACT_RULE_NETWORK using (attestation.network == membership.network)

        if (membership.network.operator != null) {
            Amend.CONTRACT_RULE_OPERATOR_ATTESTATION using (attestation.attestor == membership.network.operator)
        }
    }
}