package io.onixlabs.corda.bnms.contract

import net.corda.core.serialization.CordaSerializable
import java.util.*

/**
 * Represents a role possessed by a network member.
 *
 * @property name The name of the role.
 * @property normalizedName The normalize name of the role.
 */
@CordaSerializable
class Role(val name: String) {

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

    val normalizedName: String
        get() = name.toLowerCase()

    /**
     * Compares this object for equality with the specified object.
     *
     * @param other The object to compare with this object.
     * @return Returns true if the objects are considered equal; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return other === this || (other != null
                && other is Role
                && other.normalizedName == normalizedName)
    }

    /**
     * Serves as the default hash code implementation.
     * @return Returns a unique hash code for this object instance.
     */
    override fun hashCode() = Objects.hash(normalizedName)

    /**
     * Gets a string representation of this object instance.
     * @return Returns a string representation of this object instance.
     */
    override fun toString() = "Role: name = $name, normalized name = $normalizedName"
}