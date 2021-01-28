package io.onixlabs.corda.bnms.v1.contract.membership.general

import io.onixlabs.corda.bnms.v1.contract.DECENTRALIZED_NETWORK
import io.onixlabs.corda.bnms.v1.contract.IDENTITY_A
import io.onixlabs.corda.bnms.v1.contract.Setting
import io.onixlabs.corda.bnms.v1.contract.membership.Membership
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MembershipSettingTests {

    @Test
    fun `Membership hasSetting should return true when the expected setting is present`() {

        // Arrange
        val membership = Membership(DECENTRALIZED_NETWORK, IDENTITY_A.party).addSetting("Test", 123)

        // Act
        val result = membership.hasSetting(Setting("Test", 123))

        // Assert
        assertTrue(result)
    }

    @Test
    fun `Membership hasSetting should return true when the expected setting by property is present`() {

        // Arrange
        val membership = Membership(DECENTRALIZED_NETWORK, IDENTITY_A.party).addSetting("Test", 123)

        // Act
        val result = membership.hasSetting("Test")

        // Assert
        assertTrue(result)
    }

    @Test
    fun `Membership getSettings should return a single setting by property name`() {

        // Arrange
        val settings = setOf(Setting("One", 1), Setting("Two", 2), Setting("Three", 3))
        val membership = Membership(DECENTRALIZED_NETWORK, IDENTITY_A.party).addSettings(settings)

        // Act
        val result = membership.getSettings<Int>("One")

        // Assert
        assertEquals(1, result.size)
    }

    @Test
    fun `Membership getSettings should return multiple settings by property name`() {

        // Arrange
        val settings = setOf(Setting("One", 1), Setting("One", 2), Setting("Two", 3))
        val membership = Membership(DECENTRALIZED_NETWORK, IDENTITY_A.party).addSettings(settings)

        // Act
        val result = membership.getSettings<Int>("One")

        // Assert
        assertEquals(2, result.size)
    }

    @Test
    fun `Membership getSettings should fail because the expected type cannot be cast to the specified type`() {

        // Arrange
        val settings = setOf(Setting("One", 1), Setting("Two", 2))
        val membership = Membership(DECENTRALIZED_NETWORK, IDENTITY_A.party).addSettings(settings)

        // Act
        val result = assertThrows<ClassCastException> { membership.getSettings<Long>("One") }

        // Assert
        assertEquals("Cannot cast java.lang.Integer to java.lang.Long", result.message)
    }
}
