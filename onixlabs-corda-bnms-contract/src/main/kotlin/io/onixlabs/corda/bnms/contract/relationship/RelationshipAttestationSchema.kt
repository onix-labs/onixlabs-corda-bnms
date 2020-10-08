package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.identity.framework.contract.AttestationStatus
import net.corda.core.crypto.NullKeys.NULL_PARTY
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.*

object RelationshipAttestationSchema {

    object RelationshipAttestationSchemaV1 : MappedSchema(
        schemaFamily = RelationshipAttestationSchema.javaClass,
        version = 1,
        mappedTypes = listOf(RelationshipAttestationEntity::class.java)
    ) {
        override val migrationResource = "relationship-attestation-schema.changelog-master"
    }

    @Entity
    @Table(name = "relationship_attestation_states")
    class RelationshipAttestationEntity(
        @Column(name = "linear_id", nullable = false)
        val linearId: UUID = UUID.randomUUID(),

        @Column(name = "external_id", nullable = true)
        val externalId: String? = null,

        @Column(name = "attestor", nullable = false)
        val attestor: AbstractParty = NULL_PARTY,

        @Column(name = "pointer", nullable = false)
        val pointer: String = "",

        @Column(name = "pointer_type", nullable = false)
        val pointerType: String = "",

        @Column(name = "network_value", nullable = false)
        val networkValue: String = "",

        @Column(name = "normalized_network_value", nullable = false)
        val normalizedNetworkValue: String = "",

        @Column(name = "network_operator", nullable = true)
        val networkOperator: AbstractParty? = null,

        @Column(name = "network_hash", nullable = false)
        val networkHash: String = "",

        @Column(name = "status", nullable = false)
        @Enumerated(EnumType.STRING)
        val status: AttestationStatus = AttestationStatus.REJECTED,

        @Column(name = "hash", nullable = false, unique = true)
        val hash: String = ""
    ) : PersistentState()
}