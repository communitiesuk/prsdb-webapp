package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import java.time.LocalDate
import kotlin.test.assertEquals

class LandlordDetailsUpdateJourneyDataHelperTests {
    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        journeyDataBuilder = JourneyDataBuilder(mock())
    }

    @Test
    fun `getEmailUpdateIfPresent returns an email if the email page is in journeyData`() {
        val newEmail = "new email address value"
        val testJourneyData = journeyDataBuilder.withEmailAddressUpdate(newEmail).build()

        val emailUpdate = LandlordDetailsUpdateJourneyDataHelper.getEmailUpdateIfPresent(testJourneyData)

        assertEquals(newEmail, emailUpdate)
    }

    @Test
    fun `getEmailUpdateIfPresent returns null if the email page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val emailUpdate = LandlordDetailsUpdateJourneyDataHelper.getEmailUpdateIfPresent(testJourneyData)

        assertNull(emailUpdate)
    }

    @Test
    fun `getNameUpdateIfPresent returns a name if the name page is in journeyData`() {
        val newName = "New Name"
        val testJourneyData = journeyDataBuilder.withNameUpdate(newName).build()

        val nameUpdate = LandlordDetailsUpdateJourneyDataHelper.getNameUpdateIfPresent(testJourneyData)

        assertEquals(newName, nameUpdate)
    }

    @Test
    fun `getNameUpdateIfPresent returns null if the name page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val nameUpdate = LandlordDetailsUpdateJourneyDataHelper.getNameUpdateIfPresent(testJourneyData)

        assertNull(nameUpdate)
    }

    @Test
    fun `getAddressIfPresent returns the selected address if the selected address in in journey data`() {
        val singleLineAddress = "address passed in"
        val uprn: Long = 44
        val authority = LocalAuthority()
        val testJourneyData = journeyDataBuilder.withSelectedAddress(singleLineAddress, uprn, authority).build()

        val addressUpdate = LandlordDetailsUpdateJourneyDataHelper.getAddressIfPresent(testJourneyData)

        assertEquals(AddressDataModel(singleLineAddress, uprn = uprn, localAuthorityId = authority.id), addressUpdate)
    }

    @Test
    fun `getAddressIfPresent returns a manual address if manual address was selected and the manual address in in journey data`() {
        val lineOne = "first line"
        val locality = "a place"
        val postcode = "EG1 9ZY"
        val testJourneyData =
            journeyDataBuilder
                .withLookedUpAddresses()
                .withManualAddressSelected()
                .withManualAddress(lineOne, locality, postcode)
                .build()

        val addressUpdate = LandlordDetailsUpdateJourneyDataHelper.getAddressIfPresent(testJourneyData)

        assertEquals(
            AddressDataModel(
                AddressDataModel.manualAddressDataToSingleLineAddress(lineOne, locality, postcode),
                townName = locality,
                postcode = postcode,
            ),
            addressUpdate,
        )
    }

    @Test
    fun `getAddressIfPresent returns a manual address if lookup returned no addresses and the manual address in in journey data`() {
        val lineOne = "first line"
        val locality = "a place"
        val postcode = "EG1 9ZY"
        val testJourneyData = journeyDataBuilder.withEmptyLookedUpAddresses().withManualAddress(lineOne, locality, postcode).build()

        val addressUpdate = LandlordDetailsUpdateJourneyDataHelper.getAddressIfPresent(testJourneyData)

        assertEquals(
            AddressDataModel(
                AddressDataModel.manualAddressDataToSingleLineAddress(lineOne, locality, postcode),
                townName = locality,
                postcode = postcode,
            ),
            addressUpdate,
        )
    }

    @Test
    fun `getAddressUpdateIfPresent returns null if the address pages are not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val addressUpdate = LandlordDetailsUpdateJourneyDataHelper.getAddressIfPresent(testJourneyData)

        assertNull(addressUpdate)
    }

    @Test
    fun `getPhoneNumberIfPresent returns a phone number if the phone number page is in journeyData`() {
        val newPhoneNumber = "new phone number"
        val testJourneyData = journeyDataBuilder.withPhoneNumber(newPhoneNumber).build()

        val phoneNumberUpdate = LandlordDetailsUpdateJourneyDataHelper.getPhoneNumberIfPresent(testJourneyData)

        assertEquals(newPhoneNumber, phoneNumberUpdate)
    }

    @Test
    fun `getPhoneNumberIfPresent returns null if the phone number page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val phoneNumberUpdate = LandlordDetailsUpdateJourneyDataHelper.getPhoneNumberIfPresent(testJourneyData)

        assertNull(phoneNumberUpdate)
    }

    @Test
    fun `getDateOfBirthIfPresent returns the date if the date of birth page is in journeyData`() {
        val newDateOfBirth = LocalDate.of(1991, 1, 1)
        val testJourneyData = journeyDataBuilder.withDateOfBirthUpdate(newDateOfBirth).build()

        val dateOfBirthUpdate = LandlordDetailsUpdateJourneyDataHelper.getDateOfBirthIfPresent(testJourneyData)

        assertEquals(newDateOfBirth, dateOfBirthUpdate)
    }

    @Test
    fun `getDateOfBirthIfPresent returns null if the date of birth page is in not journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val dateOfBirthUpdate = LandlordDetailsUpdateJourneyDataHelper.getDateOfBirthIfPresent(testJourneyData)

        assertEquals(null, dateOfBirthUpdate)
    }
}
