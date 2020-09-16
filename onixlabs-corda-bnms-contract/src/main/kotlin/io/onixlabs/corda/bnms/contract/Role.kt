package io.onixlabs.corda.bnms.contract

import io.onixlabs.corda.identity.framework.contract.Claim
import java.util.*

/**
 * Represents a claim that describes a role possessed by a network member.
 *
 * @property property The property of the Role claim, which is set to "Role".
 * @property value The value of the Role claim, which is the name of the role.
 * @property normalizedValue The normalized value of the Role claim.
 */
class Role(value: String) : Claim<String>("Role", value) {

    companion object {

        /**
         * Defines the Network Operator role.
         */
        val NETWORK_OPERATOR = Role("Network Operator")

        /**
         * Defines the Administrator role.
         */
        val ADMINISTRATOR = Role("Administrator")

        /**
         * Defines the User role.
         */
        val USER = Role("User")

        /**
         * Defines the Guest role.
         */
        val GUEST = Role("Guest")
    }

    val normalizedValue: String
        get() = value.toLowerCase()

    /**
     * Compares this object for equality with the specified object.
     *
     * @param other The object to compare with this object.
     * @return Returns true if the objects are considered equal; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return other === this || (other is Role && other.normalizedValue == normalizedValue)
    }

    /**
     * Serves as the default hash code implementation.
     * @return Returns a unique hash code for this object instance.
     */
    override fun hashCode(): Int {
        return Objects.hash(normalizedValue)
    }
}