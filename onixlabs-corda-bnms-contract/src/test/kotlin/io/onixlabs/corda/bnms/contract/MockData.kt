package io.onixlabs.corda.bnms.contract

import io.onixlabs.corda.bnms.contract.DummyConfiguration.Companion.CENTRALIZED_CONFIGURATION
import io.onixlabs.corda.bnms.contract.DummyConfiguration.Companion.DECENTRALIZED_CONFIGURATION
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import net.corda.core.contracts.StateRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.CordaX500Name
import net.corda.core.serialization.CordaSerializable
import net.corda.testing.core.TestIdentity

@CordaSerializable
class DummyConfiguration(
    override val name: String,
    override val networkIdentities: Set<AbstractParty>
) : Configuration() {
    companion object {
        val DECENTRALIZED_CONFIGURATION = DummyConfiguration("Decentralized", DECENTRALIZED_PARTICIPANTS)
        val CENTRALIZED_CONFIGURATION = DummyConfiguration("Centralized A", CENTRALIZED_PARTICIPANTS)
    }
}

val IDENTITY_A = TestIdentity(CordaX500Name("PartyA", "London", "GB"))
val IDENTITY_B = TestIdentity(CordaX500Name("PartyB", "New York", "US"))
val IDENTITY_C = TestIdentity(CordaX500Name("PartyC", "Paris", "FR"))
val OPERATOR_A = TestIdentity(CordaX500Name("OperatorA", "London", "GB"))

val DECENTRALIZED_PARTICIPANTS = setOf(IDENTITY_A.party, IDENTITY_B.party, IDENTITY_C.party)
val CENTRALIZED_PARTICIPANTS = setOf(IDENTITY_A.party, IDENTITY_B.party, IDENTITY_C.party, OPERATOR_A.party)

val DECENTRALIZED_NETWORK = Network("Decentralized Network")
val CENTRALIZED_NETWORK_A = Network("Centralized Network A", OPERATOR_A.party)

val DECENTRALIZED_MEMBERSHIP_A = Membership(DECENTRALIZED_NETWORK, IDENTITY_A.party)
val DECENTRALIZED_MEMBERSHIP_B = Membership(DECENTRALIZED_NETWORK, IDENTITY_B.party)
val DECENTRALIZED_MEMBERSHIP_C = Membership(DECENTRALIZED_NETWORK, IDENTITY_C.party)

val CENTRALIZED_MEMBERSHIP_A = Membership(CENTRALIZED_NETWORK_A, IDENTITY_A.party)
val CENTRALIZED_MEMBERSHIP_B = Membership(CENTRALIZED_NETWORK_A, IDENTITY_B.party)
val CENTRALIZED_MEMBERSHIP_C = Membership(CENTRALIZED_NETWORK_A, IDENTITY_C.party)
val CENTRALIZED_MEMBERSHIP_O = Membership(CENTRALIZED_NETWORK_A, OPERATOR_A.party, roles = setOf(Role.NETWORK_OPERATOR))

val CENTRALIZED_RELATIONSHIP = Relationship(CENTRALIZED_NETWORK_A, CENTRALIZED_CONFIGURATION)
val DECENTRALIZED_RELATIONSHIP = Relationship(DECENTRALIZED_NETWORK, DECENTRALIZED_CONFIGURATION)

val REVOCATION_LOCK = RevocationLock.create(IDENTITY_A.party, DECENTRALIZED_RELATIONSHIP)

val INVALID_NETWORK = Network("Invalid Network")
val INVALID_STATEREF = StateRef(SecureHash.zeroHash, 0)