package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
        mockAddressDataService = mock()
    }

    @Test
    fun `getName returns the corresponding name (verified)`() {
        val expectedVerifiedName = "verified name"

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                LandlordRegistrationStepId.VerifyIdentity.urlPathSegment,
                "name",
            ),
        ).thenReturn(expectedVerifiedName)

        val verifiedName = LandlordJourneyDataHelper.getName(mockJourneyDataService, mockJourneyData)

        assertEquals(expectedVerifiedName, verifiedName)
    }

    @Test
    fun `getName returns the corresponding name (manual)`() {
        val expectedManualName = "manual name"

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                LandlordRegistrationStepId.Name.urlPathSegment,
                "name",
            ),
        ).thenReturn(expectedManualName)

        val manualName = LandlordJourneyDataHelper.getName(mockJourneyDataService, mockJourneyData)

        assertEquals(expectedManualName, manualName)
    }

    @Test
    fun `getDOB returns the corresponding date of birth (verified)`() {
        val expectedVerifiedDOB = LocalDate.of(2000, 1, 1)

        whenever(
            mockJourneyDataService.getFieldLocalDateValue(
                mockJourneyData,
                LandlordRegistrationStepId.VerifyIdentity.urlPathSegment,
                "birthDate",
            ),
        ).thenReturn(expectedVerifiedDOB)

        val verifiedDOB = LandlordJourneyDataHelper.getDOB(mockJourneyDataService, mockJourneyData)

        assertEquals(expectedVerifiedDOB, verifiedDOB)
    }

    @Test
    fun `getDOB returns the corresponding date of birth (manual)`() {
        val expectedManualDOB = LocalDate.of(2000, 1, 1)

        whenever(
            mockJourneyDataService.getFieldIntegerValue(
                mockJourneyData,
                LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                "day",
            ),
        ).thenReturn(expectedManualDOB.dayOfMonth)

        whenever(
            mockJourneyDataService.getFieldIntegerValue(
                mockJourneyData,
                LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                "month",
            ),
        ).thenReturn(expectedManualDOB.monthValue)

        whenever(
            mockJourneyDataService.getFieldIntegerValue(
                mockJourneyData,
                LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                "year",
            ),
        ).thenReturn(expectedManualDOB.year)

        val manualDOB = LandlordJourneyDataHelper.getDOB(mockJourneyDataService, mockJourneyData)

        assertEquals(expectedManualDOB, manualDOB)
    }

    @ParameterizedTest(name = "when livesInUK = {0}")
    @CsvSource("true", "false")
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
            mockJourneyDataService.getFieldBooleanValue(
                mockJourneyData,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
                "livesInUK",
            ),
        ).thenReturn(livesInUK)

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                selectAddressPathSegment,
                "address",
            ),
        ).thenReturn(selectedAddress)

        whenever(
            mockAddressDataService.getAddressData(selectedAddress),
        ).thenReturn(expectedAddressDataModel)

        val addressDataModel =
            LandlordJourneyDataHelper.getAddress(mockJourneyDataService, mockJourneyData, mockAddressDataService)

        assertEquals(expectedAddressDataModel, addressDataModel)
    }

    @ParameterizedTest(name = "when livesInUK = {0}")
    @CsvSource("true", "false")
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
            mockJourneyDataService.getFieldBooleanValue(
                mockJourneyData,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
                "livesInUK",
            ),
        ).thenReturn(livesInUK)

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                selectAddressPathSegment,
                "address",
            ),
        ).thenReturn(MANUAL_ADDRESS_CHOSEN)

        whenever(
            mockAddressDataService.getManualAddress(mockJourneyDataService, mockJourneyData, manualAddressPathSegment),
        ).thenReturn(expectedAddressDataModel)

        val addressDataModel =
            LandlordJourneyDataHelper.getAddress(mockJourneyDataService, mockJourneyData, mockAddressDataService)

        assertEquals(expectedAddressDataModel, addressDataModel)
    }
}
