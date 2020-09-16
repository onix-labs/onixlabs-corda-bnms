package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.bnms.contract.Setting
import io.onixlabs.corda.identity.framework.contract.StaticClaimPointer
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class RelationshipMember(
    val member: AbstractParty,
    val settings: Set<Setting<*>> = emptySet(),
    val claims: Set<StaticClaimPointer<*>> = emptySet()
)