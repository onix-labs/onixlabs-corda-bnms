package io.onixlabs.corda.bnms.contract.relationship

import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object RelationshipSchema {

    object RelationshipSchemaV1 : MappedSchema(
        schemaFamily = RelationshipSchema.javaClass,
        version = 1,
        mappedTypes = listOf(RelationshipEntity::class.java)
    ) {
        override val migrationResource = "relationship-schema.changelog-master"
    }

    @Entity
    @Table(name = "relationship_states")
    class RelationshipEntity(
        @Column(name = "linear_id", nullable = false)
        val linearId: UUID = UUID.randomUUID(),

        @Column(name = "external_id", nullable = true)
        val externalId: String? = null,

        @Column(name = "network_value", nullable = false)
        val networkValue: String = "",

        @Column(name = "normalized_network_value", nullable = false)
        val normalizedNetworkValue: String = "",

        @Column(name = "network_operator", nullable = true)
        val networkOperator: AbstractParty? = null,

        @Column(name = "network_hash", nullable = false)
        val networkHash: String = "",

        @Column(name = "hash", nullable = false, unique = true)
        val hash: String = ""
    ) : PersistentState()
}