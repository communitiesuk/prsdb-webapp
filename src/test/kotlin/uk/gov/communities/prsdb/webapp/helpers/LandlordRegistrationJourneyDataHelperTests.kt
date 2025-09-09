package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import java.time.LocalDate
import kotlin.test.assertEquals

class LandlordRegistrationJourneyDataHelperTests {
    private lateinit var mockLocalAuthorityService: LocalAuthorityService
    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        mockLocalAuthorityService = mock()
        journeyDataBuilder = JourneyDataBuilder.landlordDefault(mockLocalAuthorityService)
    }

    @Test
    fun `getName returns the corresponding name (verified)`() {
        val expectedVerifiedName = "verified name"
        val mockJourneyData = journeyDataBuilder.withVerifiedUser(expectedVerifiedName, LocalDate.of(1, 1, 1)).build()

        val verifiedName = LandlordRegistrationJourneyDataHelper.getName(mockJourneyData)

        assertEquals(expectedVerifiedName, verifiedName)
    }

    @Test
    fun `getName returns the corresponding name (manual)`() {
        val expectedManualName = "manual name"
        val mockJourneyData = journeyDataBuilder.withUnverifiedUser(expectedManualName, LocalDate.of(1, 1, 1)).build()

        val manualName = LandlordRegistrationJourneyDataHelper.getName(mockJourneyData)

        assertEquals(expectedManualName, manualName)
    }

    @Test
    fun `getDOB returns the corresponding date of birth (verified)`() {
        val expectedVerifiedDOB = LocalDate.of(2000, 1, 1)
        val mockJourneyData = journeyDataBuilder.withVerifiedUser("name", expectedVerifiedDOB).build()

        val verifiedDOB = LandlordRegistrationJourneyDataHelper.getDOB(mockJourneyData)

        assertEquals(expectedVerifiedDOB, verifiedDOB)
    }

    @Test
    fun `getDOB returns the corresponding date of birth (manual)`() {
        val expectedManualDOB = LocalDate.of(2000, 1, 1)
        val mockJourneyData = journeyDataBuilder.withUnverifiedUser("name", expectedManualDOB).build()

        val manualDOB = LandlordRegistrationJourneyDataHelper.getDOB(mockJourneyData)

        assertEquals(expectedManualDOB, manualDOB)
    }

    @Test
    fun `getAddress returns the corresponding selected address`() {
        val selectedAddress = "1 Example Address, EG1 2AB"
        val mockJourneyData = journeyDataBuilder.withSelectedAddress(selectedAddress, localAuthority = null).build()

        val expectedAddressDataModel = AddressDataModel(selectedAddress)

        val addressDataModel = LandlordRegistrationJourneyDataHelper.getAddress(mockJourneyData)

        assertEquals(expectedAddressDataModel, addressDataModel)
    }

    @Test
    fun `getAddress returns the corresponding manual address`() {
        val addressLineOne = "1 Example Address"
        val townOrCity = "Townville"
        val postcode = "EG1 2AB"
        val mockJourneyData = journeyDataBuilder.withManualAddress(addressLineOne, townOrCity, postcode).build()

        val expectedAddressDataModel = AddressDataModel.fromManualAddressData(addressLineOne, townOrCity, postcode)

        val addressDataModel = LandlordRegistrationJourneyDataHelper.getAddress(mockJourneyData)

        assertEquals(expectedAddressDataModel, addressDataModel)
    }

    @Test
    fun `isManualAddressChosen returns true if manual address is selected`() {
        val journeyData =
            journeyDataBuilder
                .withLookedUpAddresses()
                .withManualAddressSelected()
                .build()

        assertTrue(LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData))
    }

    @Test
    fun `isManualAddressChosen returns true if there are no lookedUpAddresses`() {
        val journeyData =
            journeyDataBuilder
                .withLookedUpAddresses(emptyList())
                .build()

        assertTrue(LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData))
    }

    @Test
    fun `isManualAddressChosen returns false if there are lookedUpAddresses and manual address was not selected`() {
        val journeyData =
            journeyDataBuilder
                .withSelectedAddress("1 Street Address")
                .build()

        assertFalse(LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData))
    }

    @Test
    fun `isManualAddressChosen returns true if JourneyData's lookedUpAddresses is an empty list`() {
        val journeyData = journeyDataBuilder.withEmptyLookedUpAddresses().build()

        assertTrue(LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData))
    }

    @Test
    fun `isManualAddressChosen returns false if JourneyData's lookedUpAddresses is a populated list and manual address was not selected`() {
        val journeyData = journeyDataBuilder.withSelectedAddress("1 Street Address").build()

        assertFalse(LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData))
    }
}
