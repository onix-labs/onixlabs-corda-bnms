package io.onixlabs.corda.bnms.contract.membership

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractClassName
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class MembershipContract : Contract {

    companion object {
        @JvmStatic
        val ID: ContractClassName = this::class.java.enclosingClass.canonicalName
    }

    override fun verify(tx: LedgerTransaction) {
        TODO("Not yet implemented")
    }

    interface MembershipContractCommand : CommandData {
        fun verify(tx: LedgerTransaction, signers: Set<PublicKey>)
    }

    object Issue :
        MembershipContractCommand {
        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) {
            TODO("Not yet implemented")
        }
    }

    object Amend :
        MembershipContractCommand {
        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) {
            TODO("Not yet implemented")
        }
    }

    object Revoke :
        MembershipContractCommand {
        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) {
            TODO("Not yet implemented")
        }
    }
}