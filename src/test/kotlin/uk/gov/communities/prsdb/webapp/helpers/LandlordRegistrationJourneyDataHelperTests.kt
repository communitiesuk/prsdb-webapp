package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.mockObjects.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import java.time.LocalDate
import kotlin.test.assertEquals

class LandlordRegistrationJourneyDataHelperTests {
    private lateinit var mockAddressDataService: AddressDataService
    private lateinit var mockLocalAuthorityService: LocalAuthorityService
    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        mockAddressDataService = mock()
        mockLocalAuthorityService = mock()
        journeyDataBuilder = JourneyDataBuilder.landlordDefault(mockAddressDataService, mockLocalAuthorityService)
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
    fun `getNonUKCountryOfResidence returns the corresponding country`() {
        val expectedCountryOfResidence = "US"
        val mockJourneyData =
            journeyDataBuilder
                .withInternationalAndSelectedContactAddress(
                    expectedCountryOfResidence,
                    "international address",
                    "selected address",
                ).build()

        val nonUKCountryOfResidence = LandlordRegistrationJourneyDataHelper.getNonUKCountryOfResidence(mockJourneyData)

        assertEquals(expectedCountryOfResidence, nonUKCountryOfResidence)
    }

    @Test
    fun `getNonUKCountryOfResidence returns null if the user lives in the UK`() {
        val mockJourneyData = journeyDataBuilder.build()

        val nonUKCountryOfResidence = LandlordRegistrationJourneyDataHelper.getNonUKCountryOfResidence(mockJourneyData)

        assertNull(nonUKCountryOfResidence)
    }

    @ParameterizedTest(name = "when isEnglandOrWalesResident = {0}")
    @ValueSource(booleans = [true, false])
    fun `getAddress returns the corresponding selected address`(isEnglandOrWalesResident: Boolean) {
        val selectedAddress = "1 Example Address, EG1 2AB"
        val mockJourneyData =
            journeyDataBuilder
                .withInternationalAndSelectedContactAddress(
                    "countryOfResidence",
                    "international address",
                    selectedAddress,
                ).build()
        val expectedAddressDataModel = AddressDataModel(selectedAddress)

        whenever(mockAddressDataService.getAddressData(selectedAddress)).thenReturn(expectedAddressDataModel)

        val addressDataModel = LandlordRegistrationJourneyDataHelper.getAddress(mockJourneyData, mockAddressDataService)

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
                .withInternationalAndManualContactAddress(
                    "countryofResidence",
                    "international address",
                    addressLineOne,
                    townOrCity,
                    postcode,
                ).build()
        val expectedAddressDataModel = AddressDataModel.fromManualAddressData(addressLineOne, townOrCity, postcode)

        val addressDataModel =
            LandlordRegistrationJourneyDataHelper.getAddress(mockJourneyData, mockAddressDataService)

        assertEquals(expectedAddressDataModel, addressDataModel)
    }
}
