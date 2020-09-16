package io.onixlabs.corda.bnms.contract.membership

import io.onixlabs.corda.bnms.contract.Role
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class MembershipContract : Contract {

    companion object {
        @JvmStatic
        val ID: ContractClassName = this::class.java.enclosingClass.canonicalName
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<MembershipContractCommand>()
        when (command.value) {
            is Issue, is Amend, is Revoke -> command.value.verifyCommand(tx, command.signers.toSet())
            else -> throw IllegalArgumentException("Unrecognised command: ${command.value}")
        }
    }

    interface MembershipContractCommand : CommandData {
        fun verifyCommand(transaction: LedgerTransaction, signers: Set<PublicKey>)
    }

    object Issue : MembershipContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On membership issuing, zero membership states must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On membership issuing, only one membership state must be created."

        internal const val CONTRACT_RULE_ROLE =
            "On membership issuing, a network operator must possess the network operator role."

        internal const val CONTRACT_RULE_PREVIOUS_STATE_REF =
            "On membership issuing, the previous state reference of the created membership state must be null."

        internal const val CONTRACT_RULE_SIGNERS =
            "On membership issuing, the holder of the created membership state must sign the transaction."

        override fun verifyCommand(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            val inputs = transaction.inputsOfType<Membership>()
            val outputs = transaction.outputsOfType<Membership>()

            CONTRACT_RULE_INPUTS using (inputs.isEmpty())
            CONTRACT_RULE_OUTPUTS using (outputs.size == 1)

            val output = outputs.single()

            if (output.isNetworkOperator) {
                CONTRACT_RULE_ROLE using (output.hasRole(Role.NETWORK_OPERATOR))
            }

            CONTRACT_RULE_PREVIOUS_STATE_REF using (output.previousStateRef == null)
            CONTRACT_RULE_SIGNERS using (output.holder.owningKey in signers)
        }
    }

    object Amend : MembershipContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On membership amending, only one membership state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On membership amending, only one membership state must be created."

        internal const val CONTRACT_RULE_NETWORK =
            "On membership amending, the network of the membership must not change."

        internal const val CONTRACT_RULE_HOLDER =
            "On membership amending, the holder of the membership must not change."

        internal const val CONTRACT_RULE_ROLE =
            "On membership amending, a network operator must possess the network operator role."

        internal const val CONTRACT_RULE_PREVIOUS_STATE_REF =
            "On membership amending, the previous state reference of the created membership state must be equal to the state reference of the consumed membership state."

        internal const val CONTRACT_RULE_SIGNERS =
            "On membership amending, either the holder or the network operator of the created membership state must sign the transaction."

        override fun verifyCommand(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            val inputs = transaction.inRefsOfType<Membership>()
            val outputs = transaction.outputsOfType<Membership>()

            CONTRACT_RULE_INPUTS using (inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (outputs.size == 1)

            val input = inputs.single()
            val output = outputs.single()

            CONTRACT_RULE_NETWORK using (input.state.data.network == output.network)
            CONTRACT_RULE_HOLDER using (input.state.data.holder == output.holder)

            if (output.isNetworkOperator) {
                CONTRACT_RULE_ROLE using (output.hasRole(Role.NETWORK_OPERATOR))
            }

            CONTRACT_RULE_PREVIOUS_STATE_REF using (input.ref == output.previousStateRef)
            CONTRACT_RULE_SIGNERS using (output.participants.any { it.owningKey in signers })
        }
    }

    object Revoke : MembershipContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On membership revoking, only one membership state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On membership revoking, zero membership states must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On membership revoking, either the holder or the network operator of the consumed membership state must sign the transaction."

        override fun verifyCommand(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            val inputs = transaction.inRefsOfType<Membership>()
            val outputs = transaction.outputsOfType<Membership>()

            CONTRACT_RULE_INPUTS using (inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (outputs.isEmpty())

            val input = inputs.single()

            CONTRACT_RULE_SIGNERS using (input.state.data.participants.any { it.owningKey in signers })
        }
    }
}