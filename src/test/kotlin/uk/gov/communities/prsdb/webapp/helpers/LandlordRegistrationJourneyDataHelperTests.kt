package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.getLookedUpAddresses
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
    fun `getNonEnglandOrWalesCountryOfResidence returns the corresponding country`() {
        val expectedCountryOfResidence = "US"
        val mockJourneyData =
            journeyDataBuilder
                .withNonEnglandOrWalesAndSelectedContactAddress(
                    expectedCountryOfResidence,
                    "test address",
                    "selected address",
                ).build()

        val getNonEnglandOrWalesCountryOfResidence =
            LandlordRegistrationJourneyDataHelper.getNonEnglandOrWalesCountryOfResidence(
                mockJourneyData,
            )

        assertEquals(expectedCountryOfResidence, getNonEnglandOrWalesCountryOfResidence)
    }

    @Test
    fun `getNonEnglandOrWalesCountryOfResidence returns null if the user lives in the UK`() {
        val mockJourneyData = journeyDataBuilder.build()

        val getNonEnglandOrWalesCountryOfResidence =
            LandlordRegistrationJourneyDataHelper.getNonEnglandOrWalesCountryOfResidence(
                mockJourneyData,
            )

        assertNull(getNonEnglandOrWalesCountryOfResidence)
    }

    @ParameterizedTest(name = "when isEnglandOrWalesResident = {0}")
    @ValueSource(booleans = [true, false])
    fun `getAddress returns the corresponding selected address`(isEnglandOrWalesResident: Boolean) {
        val selectedAddress = "1 Example Address, EG1 2AB"
        val mockJourneyData =
            journeyDataBuilder
                .withNonEnglandOrWalesAndSelectedContactAddress(
                    "countryOfResidence",
                    "test address",
                    selectedAddress,
                ).build()
        val expectedAddressDataModel = AddressDataModel(selectedAddress)

        val addressDataModel = LandlordRegistrationJourneyDataHelper.getAddress(mockJourneyData, mockJourneyData.getLookedUpAddresses())

        assertEquals(expectedAddressDataModel, addressDataModel)
    }

    @ParameterizedTest(name = "when isEnglandOrWalesResident = {0}")
    @ValueSource(booleans = [true, false])
    fun `getAddress returns the corresponding manual address`(isEnglandOrWalesResident: Boolean) {
        val addressLineOne = "1 Example Address"
        val townOrCity = "Townville"
        val postcode = "EG1 2AB"
        val mockJourneyData =
            journeyDataBuilder
                .withNonEnglandOrWalesAndManualContactAddress(
                    "countryofResidence",
                    "test address",
                    addressLineOne,
                    townOrCity,
                    postcode,
                ).build()
        val expectedAddressDataModel = AddressDataModel.fromManualAddressData(addressLineOne, townOrCity, postcode)

        val addressDataModel = LandlordRegistrationJourneyDataHelper.getAddress(mockJourneyData, mockJourneyData.getLookedUpAddresses())

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
    fun `isManualAddressChosen returns true if passed an empty list of lookedUpAddresses`() {
        val journeyData =
            journeyDataBuilder
                .withLookedUpAddresses()
                .build()
        val lookedUpAddresses = listOf<AddressDataModel>()

        assertTrue(LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData, lookedUpAddresses = lookedUpAddresses))
    }

    @Test
    fun `isManualAddressChosen returns false if passed a populated list of lookedUpAddresses and manual address was not selected`() {
        val journeyData =
            journeyDataBuilder
                .withSelectedAddress("1 Street Address")
                .build()
        val lookedUpAddresses =
            listOf(
                AddressDataModel("1 Street Address"),
            )

        assertFalse(LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData, lookedUpAddresses = lookedUpAddresses))
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
