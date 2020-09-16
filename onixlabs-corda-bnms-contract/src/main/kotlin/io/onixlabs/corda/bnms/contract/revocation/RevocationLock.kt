package io.onixlabs.corda.bnms.contract.revocation

import io.onixlabs.corda.bnms.contract.revocation.RevocationLockSchema.RevocationLockEntity
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockSchema.RevocationLockSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

/**
 * Represents a revocation lock that prevents revocation of a state.
 *
 * @property owner The owner of the revocation lock.
 * @property pointer A pointer to the state that requires a revocation lock.
 * @property status Specifies whether the revocation lock is locked or unlocked.
 * @property participants The participants of the revocation lock, which is only the owner.
 */
@BelongsToContract(RevocationLockContract::class)
data class RevocationLock<T : LinearState>(
    val owner: AbstractParty,
    val pointer: RevocationLockPointer<T>,
    val status: RevocationLockStatus = RevocationLockStatus.LOCKED
) : ContractState, QueryableState {

    constructor(owner: AbstractParty, state: T) : this(
        owner,
        RevocationLockPointer(state.linearId, state.javaClass),
        RevocationLockStatus.LOCKED
    )

    override val participants: List<AbstractParty>
        get() = listOf(owner)

    /**
     * Maps this state to a persistent state.
     */
    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is RevocationLockSchemaV1 -> RevocationLockEntity(
            linearId = pointer.linearId.id,
            externalId = pointer.linearId.externalId,
            owner = owner,
            canonicalName = pointer.type.canonicalName,
            status = status
        )
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    /**
     * Gets a list of supported state schemas.
     */
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(RevocationLockSchemaV1)

    /**
     * Locks revocation of the associated state.
     */
    fun lock() = copy(status = RevocationLockStatus.LOCKED)

    /**
     * Unlocks revocation of the associated state.
     */
    fun unlock() = copy(status = RevocationLockStatus.UNLOCKED)
}