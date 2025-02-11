package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.mockObjects.JourneyDataBuilder
import java.util.Optional
import kotlin.test.assertEquals

class UpdateDetailsJourneyDataExtensionsTests {
    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        journeyDataBuilder = JourneyDataBuilder(mock(), mock())
    }

    @Test
    fun `getEmailUpdateIfPresent returns an optional email if the email page is in journeyData`() {
        val newEmail = "new email address value"
        val testJourneyData = journeyDataBuilder.withEmailAddressUpdate(newEmail).build()

        val emailUpdate = testJourneyData.getEmailUpdateIfPresent()

        assertEquals(Optional.of(newEmail), emailUpdate)
    }

    @Test
    fun `getEmailUpdateIfPresent returns an empty optional if the email page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val emailUpdate = testJourneyData.getEmailUpdateIfPresent()

        assertEquals(Optional.empty(), emailUpdate)
    }
}
