package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.NetworkState
import io.onixlabs.corda.bnms.contract.Setting
import io.onixlabs.corda.bnms.contract.identityHash
import io.onixlabs.corda.bnms.contract.relationship.RelationshipSchema.RelationshipEntity
import io.onixlabs.corda.bnms.contract.relationship.RelationshipSchema.RelationshipSchemaV1
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.identity.framework.contract.Hashable
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState

@BelongsToContract(RelationshipContract::class)
data class Relationship(
    override val network: Network,
    val members: Set<RelationshipMember> = emptySet(),
    val settings: Set<Setting<*>> = emptySet(),
    internal val previousStateRef: StateRef? = null,
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : NetworkState, Hashable {

    override val hash: SecureHash
        get() = SecureHash.sha256("${network.hash}${participants.identityHash}$previousStateRef")

    override val participants: List<AbstractParty>
        get() = members.map { it.member }

    fun createRevocationLocks(): List<RevocationLock<Relationship>> {
        return members.map { RevocationLock(it.member, this) }
    }

    /**
     * Maps this state to a persistent state.
     */
    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is RelationshipSchemaV1 -> RelationshipEntity(
            linearId = linearId.id,
            externalId = linearId.externalId,
            networkValue = network.value,
            normalizedNetworkValue = network.normalizedValue,
            networkOperator = network.operator,
            networkHash = network.hash.toString(),
            hash = hash.toString()
        )
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    /**
     * Gets a list of supported state schemas.
     */
    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(RelationshipSchemaV1)
    }
}