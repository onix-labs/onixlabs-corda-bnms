package io.onixlabs.corda.bnms.contract

import io.onixlabs.corda.identity.framework.contract.Claim
import io.onixlabs.corda.identity.framework.contract.Hashable
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import java.util.*

/**
 * Represents a claim that describes the identity of a business network.
 *
 * @property property The property of the Network claim, which is set to "Network".
 * @property value The value of the Network claim, which is the name of the business network.
 * @property normalizedValue The normalized value of the Network claim.
 * @property operator The identity of the business network operator.
 * @property hash The hash that uniquely identifies the identity of the business network.
 */
class Network(value: String, val operator: AbstractParty? = null) : Claim<String>("Network", value), Hashable {

    val normalizedValue: String
        get() = value.toLowerCase()

    override val hash: SecureHash
        get() = SecureHash.sha256("$normalizedValue$operator")

    /**
     * Compares this object for equality with the specified object.
     *
     * @param other The object to compare with this object.
     * @return Returns true if the objects are considered equal; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return this === other || (other is Network && other.hash == hash)
    }

    /**
     * Serves as the default hash code implementation.
     * @return Returns a unique hash code for this object instance.
     */
    override fun hashCode(): Int {
        return Objects.hash(hash)
    }
}