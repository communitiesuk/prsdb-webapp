package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.mockObjects.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import kotlin.test.assertEquals

class UpdateDetailsJourneyDataHelperTests {
    private lateinit var journeyDataBuilder: JourneyDataBuilder
    private lateinit var addressDataService: AddressDataService

    @BeforeEach
    fun setup() {
        addressDataService = mock()
        journeyDataBuilder = JourneyDataBuilder(addressDataService, mock())
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

        assertNull(emailUpdate)
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

        assertNull(nameUpdate)
    }

    @Test
    fun `getAddressIfPresent returns the selected address if the selected address in in journey data`() {
        val singleLineAddress = "address passed in"
        val uprn: Long = 44
        val authority = LocalAuthority()
        val testJourneyData = journeyDataBuilder.withSelectedAddress(singleLineAddress, uprn, authority).build()

        val addressUpdate = UpdateLandlordDetailsJourneyDataHelper.getAddressIfPresent(testJourneyData, addressDataService)

        assertEquals(AddressDataModel(singleLineAddress, uprn = uprn, localAuthorityId = authority.id), addressUpdate)
    }

    @Test
    fun `getAddressIfPresent returns a manual address if the manual address in in journey data`() {
        val lineOne = "first line"
        val locality = "a place"
        val postcode = "EG1 9ZY"
        val testJourneyData = journeyDataBuilder.withManualAddress(lineOne, locality, postcode).build()

        val addressUpdate = UpdateLandlordDetailsJourneyDataHelper.getAddressIfPresent(testJourneyData, addressDataService)

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

        val addressUpdate = UpdateLandlordDetailsJourneyDataHelper.getAddressIfPresent(testJourneyData, addressDataService)

        assertNull(addressUpdate)
    }
}
