package io.onixlabs.corda.bnms.contract.membership

import io.onixlabs.corda.bnms.contract.Role
import io.onixlabs.corda.bnms.contract.VerifiedCommand
import io.onixlabs.corda.bnms.contract.contractClassName
import io.onixlabs.corda.bnms.contract.verifySingleCommand
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractClassName
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class MembershipContract : Contract {

    companion object {
        @JvmStatic
        val ID: ContractClassName = this::class.contractClassName
    }

    override fun verify(tx: LedgerTransaction) = verifySingleCommand<MembershipContractCommand>(tx)

    interface MembershipContractCommand : VerifiedCommand

    object Issue : MembershipContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On membership issuance, zero states must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On membership issuance, only one state must be created."

        internal const val CONTRACT_RULE_ROLES =
            "On membership issuance, network operators must possess the network operator role."

        internal const val CONTRACT_RULE_PREVIOUS_REF =
            "On membership issuance, the previous state reference must be null."

        internal const val CONTRACT_RULE_SIGNERS =
            "On membership issuance, only the network member must sign the transaction."

        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            CONTRACT_RULE_INPUTS using (tx.inputs.isEmpty())
            CONTRACT_RULE_OUTPUTS using (tx.outputs.size == 1)

            val membershipOutputState = tx.outputsOfType<Membership>().single()

            if (membershipOutputState.isNetworkOperator) {
                CONTRACT_RULE_ROLES using (membershipOutputState.hasRole(Role.NETWORK_OPERATOR))
            }

            CONTRACT_RULE_PREVIOUS_REF using (membershipOutputState.previousStateRef == null)
            CONTRACT_RULE_SIGNERS using (membershipOutputState.bearer.owningKey == signers.single())
        }
    }

    object Amend : MembershipContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On membership amendment, only one state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On membership amendment, only one state must be created."

        internal const val CONTRACT_RULE_NETWORK_HASH =
            "On membership amendment, the network hash must not change."

        internal const val CONTRACT_RULE_NETWORK_IDENTITY =
            "On membership amendment, the network identity must not change."

        internal const val CONTRACT_RULE_ROLES =
            "On membership amendment, network operators must possess the network operator role."

        internal const val CONTRACT_RULE_PREVIOUS_REF =
            "On membership amendment, the previous state reference must be equal to the input state reference."

        internal const val CONTRACT_RULE_SIGNERS =
            "On membership amendment, either the network member or the network operator must sign the transaction."

        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            CONTRACT_RULE_INPUTS using (tx.inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (tx.outputs.size == 1)

            val membershipInputStateAndRef = tx.inRefsOfType<Membership>().single()
            val membershipInputState = membershipInputStateAndRef.state.data
            val inputNetworkHash = membershipInputState.network.hash
            val inputNetworkIdentity = membershipInputState.bearer

            val membershipOutputState = tx.outputsOfType<Membership>().single()
            val outputNetworkHash = membershipOutputState.network.hash
            val outputNetworkIdentity = membershipOutputState.bearer

            CONTRACT_RULE_NETWORK_HASH using (inputNetworkHash == outputNetworkHash)
            CONTRACT_RULE_NETWORK_IDENTITY using (inputNetworkIdentity == outputNetworkIdentity)

            if (membershipOutputState.isNetworkOperator) {
                CONTRACT_RULE_ROLES using (membershipOutputState.hasRole(Role.NETWORK_OPERATOR))
            }

            CONTRACT_RULE_PREVIOUS_REF using (membershipOutputState.previousStateRef == membershipInputStateAndRef.ref)
            CONTRACT_RULE_SIGNERS using (membershipOutputState.participants.any { it.owningKey == signers.single() })
        }
    }

    object Revoke : MembershipContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On membership revocation, only one state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On membership revocation, zero states must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On membership revocation, either the network member or the network operator must sign the transaction."

        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            CONTRACT_RULE_INPUTS using (tx.inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (tx.outputs.isEmpty())

            val inputMembershipState = tx.inputsOfType<Membership>().single()

            CONTRACT_RULE_SIGNERS using (inputMembershipState.participants.any { it.owningKey == signers.single() })
        }
    }
}