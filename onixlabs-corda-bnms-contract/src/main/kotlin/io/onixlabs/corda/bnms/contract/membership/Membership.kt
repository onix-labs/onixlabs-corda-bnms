package io.onixlabs.corda.bnms.contract.membership

import io.onixlabs.corda.bnms.contract.Hashable
import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.NetworkState
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema.MembershipEntity
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema.MembershipSchemaV1
import io.onixlabs.corda.claims.contract.ClaimPointer
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState

@BelongsToContract(MembershipContract::class)
data class Membership(
    override val network: Network,
    val bearer: AbstractParty,
    val claims: Set<ClaimPointer> = emptySet(),
    val previousStateRef: StateRef? = null,
    override val linearId: UniqueIdentifier
) : NetworkState(), Hashable {

    val isNetworkOperator: Boolean
        get() = bearer == network.operator

    override val hash: SecureHash
        get() = SecureHash.sha256("${network.hash}$bearer$previousStateRef")

    override val participants: List<AbstractParty>
        get() = listOfNotNull(bearer, network.operator)

    /**
     * Maps this state to a persistent state.
     */
    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is MembershipSchemaV1 -> MembershipEntity(
            linearId = linearId.id,
            externalId = linearId.externalId,
            bearer = bearer,
            networkName = network.name,
            normalizedNetworkName = network.normalizedName,
            networkOperator = network.operator,
            networkHash = network.hash.toString(),
            isNetworkOperator = isNetworkOperator,
            hash = hash.toString()
        )
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    /**
     * Gets a list of supported state schemas.
     */
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(MembershipSchemaV1)
}