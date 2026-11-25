package uk.gov.communities.prsdb.webapp.database.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLettingAgentData
import java.time.Instant
import java.util.UUID

class LettingAgentAccessTests {
    @Test
    fun `constructor sets the provided fields`() {
        val token = UUID.randomUUID()
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        val access =
            MockLettingAgentData.createLettingAgentAccess(
                token = token,
                invitedEmail = "agent@example.com",
                propertyOwnership = propertyOwnership,
            )

        assertEquals(token, access.token)
        assertEquals("agent@example.com", access.invitedEmail)
        assertEquals(propertyOwnership, access.propertyOwnership)
    }

    @Test
    fun `setEncodedPassword records when the password was set`() {
        val access = MockLettingAgentData.createLettingAgentAccessWithoutPassword()
        val before = Instant.now()

        access.setEncodedPassword("encoded")

        assertEquals("encoded", access.encodedPassword)
        assertTrue(access.passwordSetDate!! in before..Instant.now())
    }
}
