package io.onixlabs.corda.bnms.contract.membership

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.NetworkState
import io.onixlabs.corda.bnms.contract.Permission
import io.onixlabs.corda.bnms.contract.Role
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema.MembershipEntity
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema.MembershipSchemaV1
import io.onixlabs.corda.identity.framework.contract.Hashable
import io.onixlabs.corda.identity.framework.contract.StaticClaimPointer
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
    val holder: AbstractParty,
    val identity: Set<StaticClaimPointer<*>> = emptySet(),
    val roles: Set<Role> = emptySet(),
    val permissions: Set<Permission> = emptySet(),
    override val linearId: UniqueIdentifier = UniqueIdentifier(),
    val previousStateRef: StateRef? = null
) : NetworkState, Hashable {

    companion object {
        @JvmStatic
        fun createMembershipHash(network: Network, holder: AbstractParty, previousStateRef: StateRef?): SecureHash {
            return SecureHash.sha256("$network$holder$previousStateRef")
        }
    }

    val isNetworkOperator: Boolean
        get() = holder == network.operator

    override val hash: SecureHash
        get() = createMembershipHash(network, holder, previousStateRef)

    override val participants: List<AbstractParty>
        get() = setOf(holder, network.operator).filterNotNull().toList()

    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is MembershipSchemaV1 -> MembershipEntity(
            linearId = linearId.id,
            externalId = linearId.externalId,
            holder = holder,
            networkValue = network.value,
            normalizedNetworkValue = network.normalizedValue,
            networkOperator = network.operator,
            networkHash = network.hash.toString(),
            isNetworkOperator = isNetworkOperator,
            hash = hash.toString()
        )
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(MembershipSchemaV1)
    }

    fun hasRole(role: Role): Boolean {
        return role in roles
    }

    fun hasRole(value: String): Boolean {
        return hasRole(Role(value))
    }

    fun addRoles(vararg roles: Role): Membership {
        return copy(roles = this.roles + roles)
    }

    fun addRoles(vararg values: String): Membership {
        return copy(roles = this.roles + values.map { Role(it) })
    }

    fun removeRoles(vararg roles: Role): Membership {
        return copy(roles = this.roles - roles)
    }

    fun removeRoles(vararg values: String): Membership {
        return copy(roles = this.roles - values.map { Role(it) })
    }

    fun getPermission(property: String): Permission? {
        return permissions.singleOrNull { it.normalizedProperty == property.toLowerCase() }
    }

    fun hasPermission(property: String): Boolean {
        return getPermission(property) != null
    }

    fun addPermission(property: String, value: String): Membership {
        return addPermissions(Permission(property, value))
    }

    fun addPermissions(vararg permissions: Permission): Membership {
        return copy(permissions = this.permissions + permissions)
    }

    fun removePermission(property: String): Membership {
        val permission = getPermission(property)
        return if (permission != null) {
            removePermissions(permission)
        } else this
    }

    fun removePermissions(vararg permissions: Permission): Membership {
        return copy(permissions = this.permissions - permissions)
    }
}