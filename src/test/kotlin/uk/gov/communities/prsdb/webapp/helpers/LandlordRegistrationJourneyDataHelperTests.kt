package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.mockObjects.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import java.time.LocalDate
import kotlin.test.assertEquals

class LandlordRegistrationJourneyDataHelperTests {
    private lateinit var mockAddressDataService: AddressDataService
    private lateinit var journeyDataBuilder: JourneyDataBuilder

    companion object {
        private const val COUNTRY_OF_RESIDENCE = "France"

        @JvmStatic
        private fun provideCountryOfResidenceFormInputs() =
            listOf(
                Arguments.of(true, null),
                Arguments.of(false, COUNTRY_OF_RESIDENCE),
            )
    }

    @BeforeEach
    fun setup() {
        mockAddressDataService = mock()
        journeyDataBuilder = JourneyDataBuilder.landlordDefault(mockAddressDataService)
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

    @ParameterizedTest(name = "when livesInUK = {0}")
    @MethodSource("provideCountryOfResidenceFormInputs")
    fun `getNonUKCountryOfResidence returns the corresponding country or null`(
        livesInUK: Boolean,
        expectedNonUKCountryOfResidence: String?,
    ) {
        val mockJourneyData =
            journeyDataBuilder
                .withInternationalAndSelectedContactAddress(
                    COUNTRY_OF_RESIDENCE,
                    "international address",
                    "selected address",
                ).build()

        val nonUKCountryOfResidence = LandlordRegistrationJourneyDataHelper.getNonUKCountryOfResidence(mockJourneyData)

        assertEquals(expectedNonUKCountryOfResidence, nonUKCountryOfResidence)
    }

    @ParameterizedTest(name = "when livesInUK = {0}")
    @ValueSource(booleans = [true, false])
    fun `getAddress returns the corresponding selected address`(livesInUK: Boolean) {
        val selectedAddress = "1 Example Address, EG1 2AB"
        val mockJourneyData =
            journeyDataBuilder.withSelectedAddress(selectedAddress, isContactAddress = !livesInUK).build()
        val expectedAddressDataModel = AddressDataModel(selectedAddress)

        whenever(mockAddressDataService.getAddressData(selectedAddress)).thenReturn(expectedAddressDataModel)

        val addressDataModel = LandlordRegistrationJourneyDataHelper.getAddress(mockJourneyData, mockAddressDataService)

        assertEquals(expectedAddressDataModel, addressDataModel)
    }

    @ParameterizedTest(name = "when livesInUK = {0}")
    @ValueSource(booleans = [true, false])
    fun `getAddress returns the corresponding manual address`(livesInUK: Boolean) {
        val addressLineOne = "1 Example Address"
        val townOrCity = "Townville"
        val postcode = "EG1 2AB"
        val mockJourneyData =
            journeyDataBuilder
                .withManualAddress(addressLineOne, townOrCity, postcode, isContactAddress = !livesInUK)
                .build()
        val expectedAddressDataModel = AddressDataModel.fromManualAddressData(addressLineOne, townOrCity, postcode)

        val addressDataModel =
            LandlordRegistrationJourneyDataHelper.getAddress(mockJourneyData, mockAddressDataService)

        assertEquals(expectedAddressDataModel, addressDataModel)
    }
}
