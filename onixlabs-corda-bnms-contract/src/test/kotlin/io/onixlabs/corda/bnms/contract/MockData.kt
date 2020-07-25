package io.onixlabs.corda.bnms.contract

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import net.corda.core.contracts.StateRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity

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

val GLOBAL_RELATIONSHIP_SETTINGS = setOf(
    Setting("string", "Hello, World!"),
    Setting("number", 123),
    Setting("boolean", true)
)

val PARTICIPANT_RELATIONSHIP_SETTINGS = mapOf(
    IDENTITY_A.party to setOf(
        Setting("string", "Hello, World!"),
        Setting("number", 123),
        Setting("boolean", true)
    ),
    IDENTITY_B.party to setOf(
        Setting("string", "Hello, World!"),
        Setting("number", 123),
        Setting("boolean", true)
    ),
    IDENTITY_C.party to setOf(
        Setting("string", "Hello, World!"),
        Setting("number", 123),
        Setting("boolean", true)
    )
)

val CENTRALIZED_RELATIONSHIP = Relationship(
    CENTRALIZED_NETWORK_A,
    GLOBAL_RELATIONSHIP_SETTINGS,
    PARTICIPANT_RELATIONSHIP_SETTINGS
)

val DECENTRALIZED_RELATIONSHIP = Relationship(
    DECENTRALIZED_NETWORK,
    GLOBAL_RELATIONSHIP_SETTINGS,
    PARTICIPANT_RELATIONSHIP_SETTINGS
)

val REVOCATION_LOCK = RevocationLock.create(IDENTITY_A.party, DECENTRALIZED_RELATIONSHIP)

val INVALID_NETWORK = Network("Invalid Network")
val INVALID_STATEREF = StateRef(SecureHash.zeroHash, 0)