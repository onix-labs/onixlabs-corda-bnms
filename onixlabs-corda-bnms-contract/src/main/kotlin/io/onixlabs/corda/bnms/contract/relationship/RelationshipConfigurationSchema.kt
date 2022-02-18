/*
 * Copyright 2020-2022 ONIXLabs
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

package io.onixlabs.corda.bnms.contract.relationship

import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object RelationshipConfigurationSchema {

    object RelationshipConfigurationSchemaV1 :
        MappedSchema(RelationshipSchema.javaClass, 1, listOf(RelationshipConfigurationEntity::class.java)) {
        override val migrationResource = "relationship-configuration-schema.changelog-master"
    }

    @Entity
    @Table(name = "relationship_configuration_states")
    class RelationshipConfigurationEntity(
        @Column(name = "linear_id", nullable = false)
        val linearId: UUID = UUID.randomUUID(),

        @Column(name = "external_id", nullable = true)
        val externalId: String? = null,

        @Column(name = "relationship_linear_id", nullable = false)
        val relationshipLinearId: UUID = UUID.randomUUID(),

        @Column(name = "relationship_external_id", nullable = true)
        val relationshipExternalId: String? = null,

        @Column(name = "network_value", nullable = false)
        val networkValue: String = "",

        @Column(name = "network_operator", nullable = true)
        val networkOperator: AbstractParty? = null,

        @Column(name = "network_hash", nullable = false)
        val networkHash: String = "",

        @Column(name = "configuration_hash", nullable = false)
        val configurationHash: String = "",

        @Column(name = "hash", nullable = false)
        val hash: String = ""
    ) : PersistentState()
}
