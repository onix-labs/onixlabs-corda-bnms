package io.onixlabs.corda.bnms.contract.revocation

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class RevocationLockContract : Contract {

    companion object {
        @JvmStatic
        val ID: ContractClassName = this::class.java.enclosingClass.canonicalName
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<RevocationLockContractCommand>()
        when (command.value) {
            is Create, is Update, is Delete -> command.value.verifyCommand(tx, command.signers.toSet())
            else -> throw IllegalArgumentException("Unrecognised command: ${command.value}")
        }
    }

    interface RevocationLockContractCommand : CommandData {
        fun verifyCommand(transaction: LedgerTransaction, signers: Set<PublicKey>)
    }

    object Create : RevocationLockContractCommand {

        internal const val CONTRACT_RULE_OUTPUTS =
            "On revocation lock creation, at least one revocation lock state must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On revocation lock creation, the owner of the revocation lock state must sign the transaction."

        override fun verifyCommand(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            val outputs = transaction.outputsOfType<RevocationLock<*>>()
            CONTRACT_RULE_OUTPUTS using (outputs.isNotEmpty())
            CONTRACT_RULE_SIGNERS using (outputs.all { it.owner.owningKey in signers })
        }
    }

    object Update : RevocationLockContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On revocation lock updating, only one revocation lock state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On revocation lock updating, only one revocation lock state must be created."

        internal const val CONTRACT_RULE_STATUS =
            "On revocation lock updating, the revocation lock status must change."

        internal const val CONTRACT_RULE_OWNER =
            "On revocation lock updating, the revocation lock owner must not change."

        internal const val CONTRACT_RULE_SIGNERS =
            "On revocation lock updating, the owner of the revocation lock state must sign the transaction."

        override fun verifyCommand(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            val inputs = transaction.inputsOfType<RevocationLock<*>>()
            val outputs = transaction.outputsOfType<RevocationLock<*>>()

            CONTRACT_RULE_INPUTS using (inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (outputs.size == 1)

            val input = inputs.single()
            val output = outputs.single()

            CONTRACT_RULE_STATUS using (input.status != output.status)
            CONTRACT_RULE_OWNER using (input.owner == output.owner)
            CONTRACT_RULE_SIGNERS using (output.owner.owningKey in signers)
        }
    }

    object Delete : RevocationLockContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On revocation lock deleting, only one revocation lock state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On revocation lock deleting, zero revocation lock states must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On revocation lock deleting, the owner of the revocation lock state must sign the transaction."

        override fun verifyCommand(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            val inputs = transaction.inputsOfType<RevocationLock<*>>()
            val outputs = transaction.outputsOfType<RevocationLock<*>>()

            CONTRACT_RULE_INPUTS using (inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (outputs.isEmpty())

            val input = inputs.single()

            CONTRACT_RULE_SIGNERS using (input.owner.owningKey in signers)
        }
    }
}