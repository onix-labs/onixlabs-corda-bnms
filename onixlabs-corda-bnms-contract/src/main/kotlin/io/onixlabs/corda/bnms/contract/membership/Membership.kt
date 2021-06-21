/*
 * Copyright 2020-2021 ONIXLabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.bnms.contract.membership

import io.onixlabs.corda.bnms.contract.*
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema.MembershipEntity
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema.MembershipSchemaV1
import io.onixlabs.corda.core.contract.Hashable
import io.onixlabs.corda.identityframework.contract.AbstractClaim
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
    val identity: Set<AbstractClaim<*>> = emptySet(),
    val settings: Set<Setting<*>> = emptySet(),
    override val linearId: UniqueIdentifier = UniqueIdentifier(),
    override val previousStateRef: StateRef? = null
) : NetworkState, Hashable {

    val isNetworkOperator: Boolean
        get() = holder == network.operator

    val roles: Set<Role>
        get() = getSettingsByType()

    val permissions: Set<Permission>
        get() = getSettingsByType()

    override val hash: SecureHash
        get() = SecureHash.sha256("$network$holder$previousStateRef")

    override val participants: List<AbstractParty>
        get() = setOf(holder, network.operator).filterNotNull()

    fun hasSetting(setting: Setting<*>): Boolean {
        return setting in settings
    }

    fun hasSetting(property: String): Boolean {
        return settings.any { it.property == property.toUpperCase() }
    }

    fun <T : Setting<*>> getSettingsByType(settingClass: Class<T>, property: String? = null): Set<T> {
        return settings
            .filter { it.javaClass == settingClass }
            .filter { property?.toUpperCase()?.equals(it.property) ?: true }
            .map { settingClass.cast(it) }
            .toSet()
    }

    inline fun <reified T : Setting<*>> getSettingsByType(property: String? = null): Set<T> {
        return getSettingsByType(T::class.java, property)
    }

    fun <T : Setting<*>> getSettingByTypeOrNull(settingClass: Class<T>, property: String? = null): T? {
        return getSettingsByType(settingClass, property).singleOrNull()
    }

    fun <T : Setting<*>> getSettingByType(settingClass: Class<T>, property: String? = null): T {
        return getSettingsByType(settingClass, property).single()
    }

    inline fun <reified T : Setting<*>> getSettingByTypeOrNull(property: String? = null): T? {
        return getSettingByTypeOrNull(T::class.java, property)
    }

    inline fun <reified T : Setting<*>> getSettingByType(property: String? = null): T {
        return getSettingByType(T::class.java, property)
    }

    fun <T : Any> getSettingsByValueType(valueClass: Class<T>, property: String? = null): Set<Setting<T>> {
        return settings
            .filter { it.value.javaClass == valueClass }
            .filter { property?.toUpperCase()?.equals(it.property) ?: true }
            .map { Setting(it.property, valueClass.cast(it.value)) }
            .toSet()
    }

    inline fun <reified T : Any> getSettingsByValueType(property: String? = null): Set<Setting<T>> {
        return getSettingsByValueType(T::class.java, property)
    }

    fun <T : Any> getSettingByValueTypeOrNull(valueClass: Class<T>, property: String? = null): Setting<T>? {
        return getSettingsByValueType(valueClass, property).singleOrNull()
    }

    fun <T : Any> getSettingByValueType(valueClass: Class<T>, property: String? = null): Setting<T> {
        return getSettingsByValueType(valueClass, property).single()
    }

    inline fun <reified T : Any> getSettingByValueTypeOrNull(property: String? = null): Setting<T>? {
        return getSettingByValueTypeOrNull(T::class.java, property)
    }

    inline fun <reified T : Any> getSettingByValueType(property: String? = null): Setting<T> {
        return getSettingByValueType(T::class.java, property)
    }

    fun <T> addSetting(property: String, value: T): Membership where T : Any {
        return addSettings(Setting(property, value))
    }

    fun <T> addSettings(settings: Set<Setting<out T>>): Membership where T : Any {
        return copy(settings = this.settings + settings)
    }

    fun <T> addSettings(vararg settings: Setting<out T>): Membership where T : Any {
        return addSettings(settings.toSet())
    }

    fun <T> removeSetting(property: String, value: T): Membership where T : Any {
        return removeSettings(Setting(property, value))
    }

    fun <T> removeSettings(settings: Set<Setting<T>>): Membership where T : Any {
        return copy(settings = this.settings - settings)
    }

    fun <T> removeSettings(vararg settings: Setting<T>): Membership where T : Any {
        return removeSettings(settings.toSet())
    }

    fun hasRole(role: Role): Boolean {
        return role in roles
    }

    fun hasRole(role: String): Boolean {
        return hasRole(Role(role))
    }

    fun addRoles(roles: Set<Role>): Membership {
        return addSettings(roles)
    }

    fun addRoles(vararg roles: Role): Membership {
        return addRoles(roles.toSet())
    }

    fun addRoles(vararg roles: String): Membership {
        return addRoles(roles.map { Role(it) }.toSet())
    }

    fun removeRoles(roles: Set<Role>): Membership {
        return removeSettings(roles)
    }

    fun removeRoles(vararg roles: Role): Membership {
        return removeRoles(roles.toSet())
    }

    fun removeRoles(vararg roles: String): Membership {
        return removeRoles(roles.map { Role(it) }.toSet())
    }

    fun hasPermission(permission: Permission): Boolean {
        return permission in permissions
    }

    fun hasPermission(permission: String): Boolean {
        return hasPermission(Permission(permission))
    }

    fun addPermissions(permissions: Set<Permission>): Membership {
        return addSettings(permissions)
    }

    fun addPermissions(vararg permissions: Permission): Membership {
        return addPermissions(permissions.toSet())
    }

    fun addPermissions(vararg permissions: String): Membership {
        return addPermissions(permissions.map { Permission(it) }.toSet())
    }

    fun removePermissions(permissions: Set<Permission>): Membership {
        return removeSettings(permissions)
    }

    fun removePermissions(vararg permissions: Permission): Membership {
        return removePermissions(permissions.toSet())
    }

    fun removePermissions(vararg permissions: String): Membership {
        return removePermissions(permissions.map { Permission(it) }.toSet())
    }

    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is MembershipSchemaV1 -> MembershipEntity(
            linearId = linearId.id,
            externalId = linearId.externalId,
            holder = holder,
            networkValue = network.value,
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

    internal fun immutableEquals(other: Membership): Boolean {
        return this === other || (network == other.network && holder == other.holder && linearId == other.linearId)
    }
}
