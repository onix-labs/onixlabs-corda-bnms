package io.onixlabs.corda.bnms.v1.contract.membership.general

import io.onixlabs.corda.bnms.v1.contract.DECENTRALIZED_NETWORK
import io.onixlabs.corda.bnms.v1.contract.IDENTITY_A
import io.onixlabs.corda.bnms.v1.contract.membership.Membership
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class MembershipPermissionTests {

    @Test
    fun `Membership hasPermission should return true when the expected permission is present`() {

        // Arrange
        val membership = Membership(DECENTRALIZED_NETWORK, IDENTITY_A.party).addPermissions("Test")

        // Act
        val result = membership.hasPermission("Test")

        // Assert
        assertTrue(result)
    }
}
