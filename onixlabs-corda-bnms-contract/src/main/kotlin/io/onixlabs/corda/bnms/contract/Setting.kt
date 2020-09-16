package io.onixlabs.corda.bnms.contract

import io.onixlabs.corda.identity.framework.contract.Claim
import java.util.*

/**
 * Represents a claim that describes a setting in a relationship.
 *
 * @property property The property of the Setting claim.
 * @property value The value of the Setting claim.
 * @property normalizedProperty The normalized value of the Setting claim.
 */
class Setting<T : Any>(property: String, value: T) : Claim<T>(property, value) {

    val normalizedProperty: String
        get() = property.toLowerCase()

    /**
     * Compares this object for equality with the specified object.
     *
     * @param other The object to compare with this object.
     * @return Returns true if the objects are considered equal; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return this === other || (other is Setting<*>
                && other.normalizedProperty == normalizedProperty
                && other.value == value)
    }

    /**
     * Serves as the default hash code implementation.
     * @return Returns a unique hash code for this object instance.
     */
    override fun hashCode(): Int {
        return Objects.hash(normalizedProperty, value)
    }
}