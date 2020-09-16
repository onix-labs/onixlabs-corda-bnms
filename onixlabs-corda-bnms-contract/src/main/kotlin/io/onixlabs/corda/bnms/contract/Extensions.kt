package io.onixlabs.corda.bnms.contract

import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty

val Iterable<AbstractParty>.identityHash: SecureHash
    get() = SecureHash.sha256(toSortedSet(IdentityComparator).joinToString())

private object IdentityComparator : Comparator<AbstractParty> {
    override fun compare(p0: AbstractParty?, p1: AbstractParty?): Int {
        return (p0?.hashCode() ?: 0).compareTo(p1?.hashCode() ?: 0)
    }
}