package io.onixlabs.corda.bnms.contract

import io.onixlabs.corda.identity.framework.contract.Claim
import java.util.*

/**
 * Represents a claim that describes a permission possessed by a network member.
 *
 * @property property The property of the Permission claim.
 * @property value The value of the Permission claim.
 * @property normalizedProperty The normalized value of the Permission claim.
 * @property normalizedValue The normalized value of the Permission claim.
 */
class Permission(property: String, value: String) : Claim<String>(property, value) {

    val normalizedProperty: String
        get() = property.toLowerCase()

    val normalizedValue: String
        get() = value.toLowerCase()

    /**
     * Compares this object for equality with the specified object.
     *
     * @param other The object to compare with this object.
     * @return Returns true if the objects are considered equal; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return this === other || (other is Permission
                && other.normalizedProperty == normalizedProperty
                && other.normalizedValue == normalizedValue)
    }

    /**
     * Serves as the default hash code implementation.
     * @return Returns a unique hash code for this object instance.
     */
    override fun hashCode(): Int {
        return Objects.hash(normalizedProperty, normalizedValue)
    }
}