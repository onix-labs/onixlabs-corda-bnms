package io.onixlabs.corda.bnms.contract

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class HashableTests {

    @Test
    fun `Identical Network instances produce the same hash`() {

        // Arrange
        val a = Network("Test Network")
        val b = Network("test network")

        // Assert
        assertEquals(a.hash, b.hash)
    }

    @Test
    fun `Identical MembershipState instances produce the same hash`() {

        // Arrange
        val a = Membership(CENTRALIZED_NETWORK_A, IDENTITY_A.party)
        val b = Membership(CENTRALIZED_NETWORK_A, IDENTITY_A.party)

        // Assert
        assertEquals(a.hash, b.hash)
    }

    @Test
    fun `Identical RelationshipState instances produce the same hash`() {

        // Arrange
        val a = Relationship(CENTRALIZED_NETWORK_A, GLOBAL_RELATIONSHIP_SETTINGS, PARTICIPANT_RELATIONSHIP_SETTINGS)
        val b = Relationship(CENTRALIZED_NETWORK_A, GLOBAL_RELATIONSHIP_SETTINGS, PARTICIPANT_RELATIONSHIP_SETTINGS)

        // Assert
        assertEquals(a.hash, b.hash)
    }
}