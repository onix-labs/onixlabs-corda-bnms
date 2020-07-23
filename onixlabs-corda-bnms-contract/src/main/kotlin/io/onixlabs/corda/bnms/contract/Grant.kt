package io.onixlabs.corda.bnms.contract

import net.corda.core.serialization.CordaSerializable
import java.util.*

/**
 * Represents a grant possessed by a network member.
 *
 * @property key The key of the grant.
 * @property value The value of the grant.
 * @property normalizedKey The normalized key of the grant.
 * @property normalizedValue The normalized value of the grant.
 * @property hash A SHA-256 hashed representation of the grant.
 */
@CordaSerializable
class Grant(val key: String, val value: String) {

    val normalizedKey: String
        get() = key.toLowerCase()

    val normalizedValue: String
        get() = value.toLowerCase()

    /**
     * Compares this object for equality with the specified object.
     *
     * @param other The object to compare with this object.
     * @return Returns true if the objects are considered equal; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return other === this || (other != null
                && other is Grant
                && other.normalizedKey == normalizedKey
                && other.normalizedValue == normalizedValue)
    }

    /**
     * Serves as the default hash code implementation.
     * @return Returns a unique hash code for this object instance.
     */
    override fun hashCode() = Objects.hash(normalizedKey, normalizedValue)

    /**
     * Gets a string representation of this object instance.
     * @return Returns a string representation of this object instance.
     */
    override fun toString() =
        "Grant: key = $key, normalized key = $normalizedKey, value = $value, normalized value = $normalizedValue"
}