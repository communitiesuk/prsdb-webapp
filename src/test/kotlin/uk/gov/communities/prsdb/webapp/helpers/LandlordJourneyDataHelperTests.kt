package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import java.time.LocalDate
import kotlin.test.assertEquals

class LandlordJourneyDataHelperTests {
    private lateinit var mockJourneyDataService: JourneyDataService
    private lateinit var mockAddressDataService: AddressDataService

    private val mockJourneyData: JourneyData = mutableMapOf()

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
        mockJourneyDataService = mock()
        mockAddressDataService = mock()
    }

    @Test
    fun `getName returns the corresponding name (verified)`() {
        val expectedVerifiedName = "verified name"

        whenever(
            JourneyDataService.getFieldStringValue(
                mockJourneyData,
                LandlordRegistrationStepId.VerifyIdentity.urlPathSegment,
                "name",
            ),
        ).thenReturn(expectedVerifiedName)

        val verifiedName = LandlordJourneyDataHelper.getName(mockJourneyData)

        assertEquals(expectedVerifiedName, verifiedName)
    }

    @Test
    fun `getName returns the corresponding name (manual)`() {
        val expectedManualName = "manual name"

        whenever(
            JourneyDataService.getFieldStringValue(
                mockJourneyData,
                LandlordRegistrationStepId.Name.urlPathSegment,
                "name",
            ),
        ).thenReturn(expectedManualName)

        val manualName = LandlordJourneyDataHelper.getName(mockJourneyData)

        assertEquals(expectedManualName, manualName)
    }

    @Test
    fun `getDOB returns the corresponding date of birth (verified)`() {
        val expectedVerifiedDOB = LocalDate.of(2000, 1, 1)

        whenever(
            JourneyDataService.getFieldLocalDateValue(
                mockJourneyData,
                LandlordRegistrationStepId.VerifyIdentity.urlPathSegment,
                "birthDate",
            ),
        ).thenReturn(expectedVerifiedDOB)

        val verifiedDOB = LandlordJourneyDataHelper.getDOB(mockJourneyData)

        assertEquals(expectedVerifiedDOB, verifiedDOB)
    }

    @Test
    fun `getDOB returns the corresponding date of birth (manual)`() {
        val expectedManualDOB = LocalDate.of(2000, 1, 1)

        whenever(
            JourneyDataService.getFieldIntegerValue(
                mockJourneyData,
                LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                "day",
            ),
        ).thenReturn(expectedManualDOB.dayOfMonth)

        whenever(
            JourneyDataService.getFieldIntegerValue(
                mockJourneyData,
                LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                "month",
            ),
        ).thenReturn(expectedManualDOB.monthValue)

        whenever(
            JourneyDataService.getFieldIntegerValue(
                mockJourneyData,
                LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                "year",
            ),
        ).thenReturn(expectedManualDOB.year)

        val manualDOB = LandlordJourneyDataHelper.getDOB(mockJourneyData)

        assertEquals(expectedManualDOB, manualDOB)
    }

    @ParameterizedTest(name = "when livesInUK = {0}")
    @MethodSource("provideCountryOfResidenceFormInputs")
    fun `getNonUKCountryOfResidence returns the corresponding country or null`(
        livesInUK: Boolean,
        expectedNonUKCountryOfResidence: String?,
    ) {
        whenever(
            JourneyDataService.getFieldBooleanValue(
                mockJourneyData,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
                "livesInUK",
            ),
        ).thenReturn(livesInUK)

        whenever(
            JourneyDataService.getFieldStringValue(
                mockJourneyData,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
                "countryOfResidence",
            ),
        ).thenReturn(COUNTRY_OF_RESIDENCE)

        val nonUKCountryOfResidence =
            LandlordJourneyDataHelper.getNonUKCountryOfResidence(mockJourneyData)

        assertEquals(expectedNonUKCountryOfResidence, nonUKCountryOfResidence)
    }

    @ParameterizedTest(name = "when livesInUK = {0}")
    @ValueSource(booleans = [true, false])
    fun `getAddress returns the corresponding selected address`(livesInUK: Boolean) {
        val selectAddressPathSegment =
            if (livesInUK) {
                LandlordRegistrationStepId.SelectAddress.urlPathSegment
            } else {
                LandlordRegistrationStepId.SelectContactAddress.urlPathSegment
            }

        val selectedAddress = "1 Example Address, EG1 2AB"

        val expectedAddressDataModel = AddressDataModel(selectedAddress)

        whenever(
            JourneyDataService.getFieldBooleanValue(
                mockJourneyData,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
                "livesInUK",
            ),
        ).thenReturn(livesInUK)

        whenever(
            JourneyDataService.getFieldStringValue(
                mockJourneyData,
                selectAddressPathSegment,
                "address",
            ),
        ).thenReturn(selectedAddress)

        whenever(
            mockAddressDataService.getAddressData(selectedAddress),
        ).thenReturn(expectedAddressDataModel)

        val addressDataModel =
            LandlordJourneyDataHelper.getAddress(mockJourneyData, mockAddressDataService)

        assertEquals(expectedAddressDataModel, addressDataModel)
    }

    @ParameterizedTest(name = "when livesInUK = {0}")
    @ValueSource(booleans = [true, false])
    fun `getAddress returns the corresponding manual address`(livesInUK: Boolean) {
        val selectAddressPathSegment =
            if (livesInUK) {
                LandlordRegistrationStepId.SelectAddress.urlPathSegment
            } else {
                LandlordRegistrationStepId.SelectContactAddress.urlPathSegment
            }

        val manualAddressPathSegment =
            if (livesInUK) {
                LandlordRegistrationStepId.ManualAddress.urlPathSegment
            } else {
                LandlordRegistrationStepId.ManualContactAddress.urlPathSegment
            }

        val addressLineOne = "1 Example Address"
        val townOrCity = "Townville"
        val postcode = "EG1 2AB"
        val expectedAddressDataModel = AddressDataModel.fromManualAddressData(addressLineOne, townOrCity, postcode)

        whenever(
            JourneyDataService.getFieldBooleanValue(
                mockJourneyData,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
                "livesInUK",
            ),
        ).thenReturn(livesInUK)

        whenever(
            JourneyDataService.getFieldStringValue(
                mockJourneyData,
                selectAddressPathSegment,
                "address",
            ),
        ).thenReturn(MANUAL_ADDRESS_CHOSEN)

        whenever(
            JourneyDataService.getFieldStringValue(
                mockJourneyData,
                manualAddressPathSegment,
                "addressLineOne",
            ),
        ).thenReturn(addressLineOne)

        whenever(
            JourneyDataService.getFieldStringValue(
                mockJourneyData,
                manualAddressPathSegment,
                "townOrCity",
            ),
        ).thenReturn(townOrCity)

        whenever(
            JourneyDataService.getFieldStringValue(
                mockJourneyData,
                manualAddressPathSegment,
                "postcode",
            ),
        ).thenReturn(postcode)

        val addressDataModel =
            LandlordJourneyDataHelper.getAddress(mockJourneyData, mockAddressDataService)

        assertEquals(expectedAddressDataModel, addressDataModel)
    }
}
