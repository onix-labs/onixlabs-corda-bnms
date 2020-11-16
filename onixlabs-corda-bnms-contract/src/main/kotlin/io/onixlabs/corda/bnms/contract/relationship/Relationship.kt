/**
 * Copyright 2020 Matthew Layton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.bnms.contract.*
import io.onixlabs.corda.bnms.contract.relationship.RelationshipSchema.RelationshipEntity
import io.onixlabs.corda.bnms.contract.relationship.RelationshipSchema.RelationshipSchemaV1
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.identityframework.contract.Hashable
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearPointer
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState

@BelongsToContract(RelationshipContract::class)
data class Relationship(
    override val network: Network,
    val members: Set<RelationshipMember>,
    val settings: Set<Setting<*>> = emptySet(),
    override val linearId: UniqueIdentifier = UniqueIdentifier(),
    override val previousStateRef: StateRef? = null
) : NetworkState, Hashable {

    init {
        check(members.isDistinctBy { it.member }) {
            "Cannot create a relationship with duplicate members."
        }

        check(settings.isDistinctBy { it.property }) {
            "Cannot create a relationship with duplicate settings."
        }
    }

    override val hash: SecureHash
        get() = SecureHash.sha256("$network${participants.identityHash}$previousStateRef")

    override val participants: List<AbstractParty>
        get() = members.map { it.member }.distinct()

    fun createRevocationLocks(): List<RevocationLock<Relationship>> {
        return members.map { RevocationLock(it.member, LinearPointer(this.linearId, this.javaClass, false)) }
    }

    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is RelationshipSchemaV1 -> RelationshipEntity(
            linearId = linearId.id,
            externalId = linearId.externalId,
            networkValue = network.value,
            networkOperator = network.operator,
            networkHash = network.hash.toString(),
            hash = hash.toString()
        )
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(RelationshipSchemaV1)
    }

    fun <T : Any> addSettings(vararg settings: Setting<T>): Relationship {
        return copy(settings = this.settings + settings)
    }

    fun <T : Any> addSetting(property: String, value: T): Relationship {
        return addSettings(Setting(property, value))
    }

    fun <T : Any> removeSettings(vararg settings: Setting<T>): Relationship {
        return copy(settings = this.settings - settings)
    }

    inline fun <reified T : Setting<*>> getSetting(property: String): T? {
        return settings.filterIsInstance<T>().singleOrNull { it.property == property.toUpperCase() }
    }

    inline fun <reified T : Any> getSettingByValueType(property: String): Setting<T>? {
        return settings.singleOrNull { it.property == property.toUpperCase() }?.cast()
    }

    internal fun immutableEquals(other: Relationship): Boolean {
        return this === other || (network == other.network && linearId == other.linearId)
    }
}
