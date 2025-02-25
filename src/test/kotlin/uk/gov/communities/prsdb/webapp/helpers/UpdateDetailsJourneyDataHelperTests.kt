package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.mockObjects.JourneyDataBuilder
import kotlin.test.assertEquals

class UpdateDetailsJourneyDataHelperTests {
    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        journeyDataBuilder = JourneyDataBuilder(mock(), mock())
    }

    @Test
    fun `getEmailUpdateIfPresent returns an email if the email page is in journeyData`() {
        val newEmail = "new email address value"
        val testJourneyData = journeyDataBuilder.withEmailAddressUpdate(newEmail).build()

        val emailUpdate = UpdateLandlordDetailsJourneyDataHelper.getEmailUpdateIfPresent(testJourneyData)

        assertEquals(newEmail, emailUpdate)
    }

    @Test
    fun `getEmailUpdateIfPresent returns null if the email page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val emailUpdate = UpdateLandlordDetailsJourneyDataHelper.getEmailUpdateIfPresent(testJourneyData)

        assertEquals(null, emailUpdate)
    }

    @Test
    fun `getNameUpdateIfPresent returns a name if the name page is in journeyData`() {
        val newName = "New Name"
        val testJourneyData = journeyDataBuilder.withNameUpdate(newName).build()

        val nameUpdate = UpdateLandlordDetailsJourneyDataHelper.getNameUpdateIfPresent(testJourneyData)

        assertEquals(newName, nameUpdate)
    }

    @Test
    fun `getNameUpdateIfPresent returns null if the name page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val nameUpdate = UpdateLandlordDetailsJourneyDataHelper.getNameUpdateIfPresent(testJourneyData)

        assertEquals(null, nameUpdate)
    }

    @Test
    fun `getPhoneNumberIfPresent returns a phone number if the phone number page is in journeyData`() {
        val newPhoneNumber = "new phone number"
        val testJourneyData = journeyDataBuilder.withPhoneNumber(newPhoneNumber).build()

        val phoneNumberUpdate = UpdateLandlordDetailsJourneyDataHelper.getPhoneNumberIfPresent(testJourneyData)

        assertEquals(newPhoneNumber, phoneNumberUpdate)
    }

    @Test
    fun `getPhoneNumberIfPresent returns null if the phone number page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val phoneNumberUpdate = UpdateLandlordDetailsJourneyDataHelper.getPhoneNumberIfPresent(testJourneyData)

        assertEquals(null, phoneNumberUpdate)
    }
}
