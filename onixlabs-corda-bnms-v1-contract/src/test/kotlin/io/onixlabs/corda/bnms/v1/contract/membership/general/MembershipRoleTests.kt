package io.onixlabs.corda.bnms.v1.contract.membership.general

import io.onixlabs.corda.bnms.v1.contract.DECENTRALIZED_NETWORK
import io.onixlabs.corda.bnms.v1.contract.IDENTITY_A
import io.onixlabs.corda.bnms.v1.contract.membership.Membership
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class MembershipRoleTests {

    @Test
    fun `Membership hasRole should return true when the expected role is present`() {

        // Arrange
        val membership = Membership(DECENTRALIZED_NETWORK, IDENTITY_A.party).addRoles("Test")

        // Act
        val result = membership.hasRole("Test")

        // Assert
        assertTrue(result)
    }
}
