package io.onixlabs.corda.bnms.contract

import net.corda.core.serialization.CordaSerializable
import java.util.*

@CordaSerializable
class Setting private constructor(val key: String, val value: Any, val type: Class<*>) {

    constructor(key: String, value: Any) : this(key, value, value.javaClass)

    val normalizedKey: String
        get() = key.toLowerCase()

    fun <T : Any> cast(type: Class<T>): T {
        if (type != this.type) {
            throw IllegalArgumentException("The specified class is invalid. Expected class is $type.")
        }

        return type.cast(value)
    }

    inline fun <reified T : Any> cast(): T = cast(T::class.java)

    /**
     * Compares this object for equality with the specified object.
     *
     * @param other The object to compare with this object.
     * @return Returns true if the objects are considered equal; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return other === this || (other != null
                && other is Role
                && other.normalizedName == normalizedKey)
    }

    /**
     * Serves as the default hash code implementation.
     * @return Returns a unique hash code for this object instance.
     */
    override fun hashCode() = Objects.hash(normalizedKey)

    /**
     * Gets a string representation of this object instance.
     * @return Returns a string representation of this object instance.
     */
    override fun toString() = "Setting: name = $key, normalized name = $normalizedKey, value = $value"
}