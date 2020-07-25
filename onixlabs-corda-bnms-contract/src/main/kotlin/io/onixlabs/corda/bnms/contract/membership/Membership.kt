package io.onixlabs.corda.bnms.contract.membership

import io.onixlabs.corda.bnms.contract.*
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema.MembershipEntity
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema.MembershipSchemaV1
import io.onixlabs.corda.bnms.contract.Role
import io.onixlabs.corda.claims.contract.ClaimPointer
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState

/**
 * Represents a configurable network membership.
 *
 * The configurability of this state allows different business networks to specify requirements for managing legal
 * identity whilst maintaining interoperability with other business networks using the same underlying framework.
 *
 * This can be very useful for handling KYC checks through legal identity information and attachments and ensuring
 * that network members are appropriately authorised through roles and grants.
 *
 * When creating or amending a membership state, the transaction must only contain one output, which is the membership
 * state. This transaction needs to be propagated to all participants from which the member requires attestation.
 * Membership attestation is static because a member can update their own membership state. If attestations were
 * linear, this would pose a vulnerability, as participants would implicitly attest to updated membership states which
 * could have undesirable consequences on the network.
 *
 * @property network The identity of the network that the membership is bound to.
 * @property claims The claims that represent the identity of the network member.
 * @property roles The roles that are possessed by the network member.
 * @property grants The grants that are possessed by the network member.
 * @property previousStateRef The state ref to the previous version of the state, or null if this is this first version.
 * @property linearId The unique identifier of the membership state.
 * @property participants The network identity of the network member, and optionally the network operator.
 * @property isNetworkOperator Determines whether the network member is the network operator.
 * @property hash A SHA-256 hash that uniquely identifies this version of the state.
 */
@BelongsToContract(MembershipContract::class)
data class Membership(
    override val network: Network,
    val bearer: AbstractParty,
    val claims: Set<ClaimPointer> = emptySet(),
    val roles: Set<Role> = emptySet(),
    val grants: Set<Grant> = emptySet(),
    val previousStateRef: StateRef? = null,
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : NetworkState(), Hashable {

    companion object {
        fun createMembershipHash(
            network: Network,
            bearer: AbstractParty,
            previousStateRef: StateRef? = null
        ): SecureHash {
            return SecureHash.sha256("${network.hash}$bearer$previousStateRef")
        }
    }

    override val participants: List<AbstractParty>
        get() = listOfNotNull(bearer, network.operator)

    val isNetworkOperator: Boolean
        get() = bearer == network.operator

    override val hash: SecureHash
        get() = createMembershipHash(network, bearer, previousStateRef)

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

    /**
     * Determines whether the network member possesses a specific role.
     *
     * @param roleName The role name to check in this membership.
     * @return Returns true if the network member possesses a specific role; otherwise, false.
     */
    fun hasRole(roleName: String): Boolean = roleName.toLowerCase() in roles.map { it.normalizedName }

    /**
     * Determines whether the network member possesses a specific role.
     *
     * @param role The role to check in this membership.
     * @return Returns true if the network member possesses a specific role; otherwise, false.
     */
    fun hasRole(role: Role): Boolean = role in roles

    /**
     * Adds the specified roles to the network member's membership.
     *
     * @param roleNames The role names to add to the network member's membership.
     * @return Returns a new membership with the specified roles added.
     */
    fun addRoles(vararg roleNames: String) = copy(roles = roles + roleNames.map { Role(it) })

    /**
     * Adds the specified roles to the network member's membership.
     *
     * @param roles The roles to add to the network member's membership.
     * @return Returns a new membership with the specified roles added.
     */
    fun addRoles(vararg roles: Role) = copy(roles = this.roles + roles)

    /**
     * Removes the specified roles to the network member's membership.
     *
     * @param roleNames The role names to remove from the network member's membership.
     * @return Returns a new membership with the specified roles removed.
     */
    fun removeRoles(vararg roleNames: String) = copy(roles = roles - roleNames.map { Role(it) })

    /**
     * Removes the specified roles to the network member's membership.
     *
     * @param roles The roles to remove from the network member's membership.
     * @return Returns a new membership with the specified roles removed.
     */
    fun removeRoles(vararg roles: Role) = copy(roles = this.roles - roles)

    /**
     * Gets a grant from this membership, or null if no grant exists with the specified key.
     *
     * @param key The key of the grant from which to obtain a value.
     * @return Returns a grant from this membership, or null if no grant exists with the specified key.
     */
    fun getGrant(key: String): Grant? = grants.singleOrNull { it.normalizedKey == key.toLowerCase() }

    /**
     * Determines whether the network member possesses a specific grant.
     *
     * @param key The key of the grant to check in this membership.
     * @return Returns true if the network member possesses a specific grant; otherwise, false.
     */
    fun hasGrant(key: String) = getGrant(key) != null

    /**
     * Adds the specified grant to the network member's membership.
     *
     * @param key The key of the grant to add.
     * @param value The value of the grant to add.
     * @return Returns a new membership with the specified grant added.
     */
    fun addGrant(key: String, value: String) = copy(grants = grants + Grant(key, value))

    /**
     * Removes the specified grant to the network member's membership.
     *
     * @param key The key of the grant to remove.
     * @return Returns a new membership with the specified grant removed.
     */
    fun removeGrant(key: String) = if (getGrant(key) != null) copy(grants = grants - getGrant(key)!!) else this
}