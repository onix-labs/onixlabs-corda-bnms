package io.onixlabs.corda.bnms.contract.membership

import net.corda.core.crypto.NullKeys.NULL_PARTY
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object MembershipSchema {

    object MembershipSchemaV1 : MappedSchema(
        schemaFamily = MembershipSchema.javaClass,
        version = 1,
        mappedTypes = listOf(MembershipEntity::class.java)
    ) {
        override val migrationResource = "membership-schema.changelog-master"
    }

    @Entity
    @Table(name = "membership_states")
    class MembershipEntity(
        @Column(name = "linear_id", nullable = false)
        val linearId: UUID = UUID.randomUUID(),

        @Column(name = "external_id", nullable = true)
        val externalId: String? = null,

        @Column(name = "holder", nullable = false)
        val holder: AbstractParty = NULL_PARTY,

        @Column(name = "network_value", nullable = false)
        val networkValue: String = "",

        @Column(name = "normalized_network_value", nullable = false)
        val normalizedNetworkValue: String = "",

        @Column(name = "network_operator", nullable = true)
        val networkOperator: AbstractParty? = null,

        @Column(name = "network_hash", nullable = false)
        val networkHash: String = "",

        @Column(name = "is_network_operator", nullable = false)
        val isNetworkOperator: Boolean = false,

        @Column(name = "hash", nullable = false, unique = true)
        val hash: String = ""
    ) : PersistentState()
}