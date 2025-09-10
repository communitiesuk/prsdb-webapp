package uk.gov.communities.prsdb.webapp.forms.pages

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.CHECKING_ANSWERS_FOR_PARAMETER_NAME
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.SectionHeaderViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder.Companion.DEFAULT_ADDRESS
import java.time.LocalDate

class LandlordRegistrationCheckAnswersPageTests {
    private lateinit var page: LandlordRegistrationCheckAnswersPage
    private lateinit var localAuthorityService: LocalAuthorityService
    private lateinit var journeyDataService: JourneyDataService
    private lateinit var validator: Validator
    private lateinit var pageData: PageData
    private lateinit var prevStepUrl: String
    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        localAuthorityService = mock()
        journeyDataService = mock()
        page = LandlordRegistrationCheckAnswersPage(journeyDataService, "/redirect")
        validator = mock()
        whenever(validator.supports(any<Class<*>>())).thenReturn(true)
        pageData = mock()
        prevStepUrl = "mock"
        journeyDataBuilder = JourneyDataBuilder.landlordDefault(localAuthorityService)
    }

    private fun getSummaryListData(journeyData: JourneyData): List<SummaryListRowViewModel> {
        whenever(journeyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

        val bindingResult = page.bindDataToFormModel(validator, pageData)
        val result = page.getModelAndView(bindingResult, prevStepUrl, journeyData, SectionHeaderViewModel("any-key", 0, 0))

        val summaryListData = result.model["summaryListData"] as List<*>
        return summaryListData.filterIsInstance<SummaryListRowViewModel>()
    }

    @Test
    fun `summaryListData has the correct name and DOB (verified)`() {
        val name = "Arthur Dent"
        val dob = LocalDate.of(2001, 1, 1)
        val journeyData = journeyDataBuilder.withVerifiedUser(name, dob).build()

        val summaryListData = getSummaryListData(journeyData)

        assertEquals(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                name,
                null,
            ),
            summaryListData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.name"
            },
        )
        assertEquals(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                dob,
                null,
            ),
            summaryListData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth"
            },
        )
    }

    @Test
    fun `summaryListData has the correct name and DOB (unverified)`() {
        val name = "Arthur Dent"
        val dob = LocalDate.of(200, 1, 1)
        val journeyData = journeyDataBuilder.withUnverifiedUser(name, dob).build()

        val summaryListData = getSummaryListData(journeyData)

        assertEquals(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                name,
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    "${LandlordRegistrationStepId.Name.urlPathSegment}?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=" +
                        LandlordRegistrationStepId.Name.urlPathSegment,
                ),
            ),
            summaryListData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.name"
            },
        )
        assertEquals(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                dob,
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    "${LandlordRegistrationStepId.DateOfBirth.urlPathSegment}?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=" +
                        LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                ),
            ),
            summaryListData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth"
            },
        )
    }

    @Test
    fun `summaryListData has the correct email and phone number`() {
        val emailAddress = "example@email.com"
        val phoneNumber = "07123456789"
        val journeyData = journeyDataBuilder.withEmailAddress(emailAddress).withPhoneNumber(phoneNumber).build()

        val summaryListData = getSummaryListData(journeyData)

        assertEquals(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.email",
                emailAddress,
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    "${LandlordRegistrationStepId.Email.urlPathSegment}?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=" +
                        LandlordRegistrationStepId.Email.urlPathSegment,
                ),
            ),
            summaryListData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.email"
            },
        )
        assertEquals(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber",
                phoneNumber,
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    "${LandlordRegistrationStepId.PhoneNumber.urlPathSegment}?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=" +
                        LandlordRegistrationStepId.PhoneNumber.urlPathSegment,
                ),
            ),
            summaryListData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber"
            },
        )
    }

    @Test
    fun `summaryListData has the correct lives in UK status`() {
        val journeyData = journeyDataBuilder.build()

        val summaryListData = getSummaryListData(journeyData)

        assertEquals(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.englandOrWalesResident",
                true,
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    "${LandlordRegistrationStepId.CountryOfResidence.urlPathSegment}?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=" +
                        LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
                ),
            ),
            summaryListData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.englandOrWalesResident"
            },
        )
    }

    @Test
    fun `summaryListData has the correct selected address`() {
        val journeyData = journeyDataBuilder.build()

        val summaryListData = getSummaryListData(journeyData)

        assertEquals(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.contactAddress",
                DEFAULT_ADDRESS,
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    "${LandlordRegistrationStepId.LookupAddress.urlPathSegment}?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=" +
                        LandlordRegistrationStepId.LookupAddress.urlPathSegment,
                ),
            ),
            summaryListData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.contactAddress"
            },
        )
    }

    @Test
    fun `summaryListData has the correct manual  address`() {
        val addressLineOne = "1 Example Road"
        val townOrCity = "Townville"
        val postcode = "EG1 2BA"
        val journeyData =
            journeyDataBuilder
                .withManualAddress(addressLineOne, townOrCity, postcode)
                .build()

        val summaryListData = getSummaryListData(journeyData)

        assertEquals(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.contactAddress",
                AddressDataModel.fromManualAddressData(addressLineOne, townOrCity, postcode).singleLineAddress,
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    "${LandlordRegistrationStepId.ManualAddress.urlPathSegment}?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=" +
                        LandlordRegistrationStepId.ManualAddress.urlPathSegment,
                ),
            ),
            summaryListData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.contactAddress"
            },
        )
    }
}
