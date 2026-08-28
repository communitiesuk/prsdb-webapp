package uk.gov.communities.prsdb.webapp.database.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLettingAgentData
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
    fun `recordEncodedPassword sets the encoded password`() {
        val access = MockLettingAgentData.createLettingAgentAccessWithoutPassword()
        access.recordEncodedPassword("{bcrypt}\$2a\$10\$examplehash")
        assertEquals("{bcrypt}\$2a\$10\$examplehash", access.encodedPassword)
    }

    @Test
    fun `recordEncodedPassword throws when password already set`() {
        val access = MockLettingAgentData.createLettingAgentAccess(encodedPassword = "{bcrypt}\$2a\$10\$existing")
        assertThrows<PrsdbWebException> {
            access.recordEncodedPassword("{bcrypt}\$2a\$10\$replacement")
        }
    }
}
