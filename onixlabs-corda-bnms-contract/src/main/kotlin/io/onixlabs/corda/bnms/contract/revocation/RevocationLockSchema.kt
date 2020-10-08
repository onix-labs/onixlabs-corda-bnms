package io.onixlabs.corda.bnms.contract.revocation

import net.corda.core.crypto.NullKeys.NULL_PARTY
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.*

object RevocationLockSchema {

    object RevocationLockSchemaV1 : MappedSchema(
        schemaFamily = RevocationLockSchema.javaClass,
        version = 1,
        mappedTypes = listOf(RevocationLockEntity::class.java)
    ) {
        override val migrationResource = "revocation-lock-schema.changelog-master"
    }

    @Entity
    @Table(name = "revocation_lock_states")
    class RevocationLockEntity(
        @Column(name = "linear_id", nullable = false)
        val linearId: UUID = UUID.randomUUID(),

        @Column(name = "external_id", nullable = true)
        val externalId: String? = null,

        @Column(name = "owner", nullable = false)
        val owner: AbstractParty = NULL_PARTY,

        @Column(name = "canonical_name", nullable = false)
        val canonicalName: String = "",

        @Column(name = "status", nullable = false)
        @Enumerated(EnumType.STRING)
        val status: RevocationLockStatus = RevocationLockStatus.LOCKED
    ) : PersistentState()
}