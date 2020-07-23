package io.onixlabs.corda.bnms.contract.revocation

import net.corda.core.serialization.CordaSerializable

/**
 * Specifies the status of the revocation lock.
 */
@CordaSerializable
enum class RevocationLockStatus {

    /**
     * The revocation lock is locked.
     */
    LOCKED,

    /**
     * The revocation lock is unlocked.
     */
    UNLOCKED
}