package io.onixlabs.corda.bnms.contract

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import net.corda.core.contracts.*
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey
import java.util.*
import kotlin.reflect.KClass

interface VerifiedCommand : CommandData {
    fun verify(tx: LedgerTransaction, signers: Set<PublicKey>)
}

internal inline fun <reified T : VerifiedCommand> Contract.verifySingleCommand(tx: LedgerTransaction) {
    val command = tx.commands.requireSingleCommand<T>()
    if (command.value.javaClass.enclosingClass != this.javaClass) {
        throw IllegalArgumentException("Unknown contract command.")
    }
    command.value.verify(tx, command.signers.toSet())
}

internal val KClass<*>.contractClassName: ContractClassName
    get() {
        return if (!this.isCompanion) {
            throw IllegalArgumentException("Must be called from a companion object.")
        } else this.java.enclosingClass.canonicalName
    }

val Set<AbstractParty>.identityHash: SecureHash
    get() = SecureHash.sha256(toSortedSet(IdentityComparator).joinToString())

internal fun StateAndRef<Membership>.getNextOutput() = state.data.copy(previousStateRef = ref)

fun <T : Configuration> StateAndRef<Relationship<T>>.getNextOutput() = state.data.copy(previousStateRef = ref)

private object IdentityComparator : Comparator<AbstractParty> {
    override fun compare(p0: AbstractParty?, p1: AbstractParty?): Int {
        return (p0?.hashCode() ?: 0).compareTo(p1?.hashCode() ?: 0)
    }
}