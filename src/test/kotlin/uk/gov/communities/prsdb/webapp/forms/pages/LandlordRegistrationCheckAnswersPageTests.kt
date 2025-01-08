package uk.gov.communities.prsdb.webapp.forms.pages

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.ui.ExtendedModelMap
import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.mockObjects.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.mockObjects.JourneyDataBuilder.Companion.DEFAULT_ADDRESS
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.FormSummaryViewModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import java.time.LocalDate

class LandlordRegistrationCheckAnswersPageTests {
    private lateinit var page: LandlordRegistrationCheckAnswersPage
    private lateinit var addressService: AddressDataService
    private lateinit var validator: Validator
    private lateinit var model: Model
    private lateinit var pageData: Map<String, Any?>
    private lateinit var prevStepUrl: String
    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        addressService = mock()
        page = LandlordRegistrationCheckAnswersPage(JourneyDataService(mock(), mock(), mock(), mock()), addressService)
        validator = mock()
        whenever(validator.supports(any<Class<*>>())).thenReturn(true)
        model = ExtendedModelMap()
        pageData = mock()
        prevStepUrl = "mock"
        journeyDataBuilder = JourneyDataBuilder.landlordDefault(addressService)
    }

    private fun getFormData(journeyData: MutableMap<String, Any?>): List<FormSummaryViewModel> {
        page.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl, journeyData)

        val formData = model.asMap()["formData"] as List<*>
        return formData.filterIsInstance<FormSummaryViewModel>()
    }

    @Test
    fun `formData has the correct name and DOB (verified)`() {
        val name = "Arthur Dent"
        val dob = LocalDate.of(2001, 1, 1)
        val journeyData = journeyDataBuilder.withVerifiedUser(name, dob).build()

        val formData = getFormData(journeyData)

        assertEquals(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                name,
                null,
            ),
            formData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.name"
            },
        )
        assertEquals(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                dob,
                null,
            ),
            formData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth"
            },
        )
    }

    @Test
    fun `formData has the correct name and DOB (unverified)`() {
        val name = "Arthur Dent"
        val dob = LocalDate.of(200, 1, 1)
        val journeyData = journeyDataBuilder.withUnverifiedUser(name, dob).build()

        val formData = getFormData(journeyData)

        assertEquals(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                name,
                LandlordRegistrationStepId.Name.urlPathSegment,
            ),
            formData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.name"
            },
        )
        assertEquals(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                dob,
                LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
            ),
            formData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth"
            },
        )
    }

    @Test
    fun `formData has the correct email and phone number`() {
        val emailAddress = "example@email.com"
        val phoneNumber = "07123456789"
        val journeyData = journeyDataBuilder.withEmailAddress(emailAddress).withPhoneNumber(phoneNumber).build()

        val formData = getFormData(journeyData)

        assertEquals(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.email",
                emailAddress,
                LandlordRegistrationStepId.Email.urlPathSegment,
            ),
            formData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.email"
            },
        )
        assertEquals(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber",
                phoneNumber,
                LandlordRegistrationStepId.PhoneNumber.urlPathSegment,
            ),
            formData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber"
            },
        )
    }

    @Test
    fun `formData has the correct lives in UK status`() {
        val journeyData = journeyDataBuilder.build()

        val formData = getFormData(journeyData)

        assertEquals(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.ukResident",
                true,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
            ),
            formData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.ukResident"
            },
        )
    }

    @Test
    fun `formData has the correct selected address`() {
        val journeyData = journeyDataBuilder.build()

        val formData = getFormData(journeyData)

        assertEquals(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.contactAddress",
                DEFAULT_ADDRESS,
                LandlordRegistrationStepId.LookupAddress.urlPathSegment,
            ),
            formData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.contactAddress"
            },
        )
    }

    @Test
    fun `formData has the correct manual  address`() {
        val addressLineOne = "1 Example Road"
        val townOrCity = "Townville"
        val postcode = "EG1 2BA"
        val journeyData =
            journeyDataBuilder
                .withManualAddress(addressLineOne, townOrCity, postcode)
                .build()

        val formData = getFormData(journeyData)

        assertEquals(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.contactAddress",
                AddressDataModel.fromManualAddressData(addressLineOne, townOrCity, postcode).singleLineAddress,
                LandlordRegistrationStepId.ManualAddress.urlPathSegment,
            ),
            formData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.contactAddress"
            },
        )
    }

    @Test
    fun `formData has the correct international and selected contact addresses`() {
        val countryOfResidence = "Germany"
        val internationalAddress = "international address"
        val selectedAddress = "1 Example Road"
        val journeyData =
            journeyDataBuilder
                .withInternationalAndSelectedContactAddress(
                    countryOfResidence,
                    internationalAddress,
                    selectedAddress,
                ).build()

        val formData = getFormData(journeyData)

        assertEquals(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.countryOfResidence",
                countryOfResidence,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
            ),
            formData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.countryOfResidence"
            },
        )
        assertEquals(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.contactAddressOutsideUK",
                internationalAddress,
                LandlordRegistrationStepId.InternationalAddress.urlPathSegment,
            ),
            formData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.contactAddressOutsideUK"
            },
        )
        assertEquals(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.ukContactAddress",
                selectedAddress,
                LandlordRegistrationStepId.LookupContactAddress.urlPathSegment,
            ),
            formData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.ukContactAddress"
            },
        )
    }

    @Test
    fun `formData has the correct international and manual contact addresses`() {
        val countryOfResidence = "Germany"
        val internationalAddress = "international address"
        val addressLineOne = "1 Example Road"
        val townOrCity = "Townville"
        val postcode = "EG1 2BA"
        val journeyData =
            journeyDataBuilder
                .withInternationalAndManualContactAddress(
                    countryOfResidence,
                    internationalAddress,
                    addressLineOne,
                    townOrCity,
                    postcode,
                ).build()

        val formData = getFormData(journeyData)

        assertEquals(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.countryOfResidence",
                countryOfResidence,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
            ),
            formData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.countryOfResidence"
            },
        )
        assertEquals(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.contactAddressOutsideUK",
                internationalAddress,
                LandlordRegistrationStepId.InternationalAddress.urlPathSegment,
            ),
            formData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.contactAddressOutsideUK"
            },
        )
        assertEquals(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.ukContactAddress",
                AddressDataModel.fromManualAddressData(addressLineOne, townOrCity, postcode).singleLineAddress,
                LandlordRegistrationStepId.ManualContactAddress.urlPathSegment,
            ),
            formData.single {
                it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.ukContactAddress"
            },
        )
    }

    @Test
    fun `formData does not contain country of residence for national landlords`() {
        val journeyData = journeyDataBuilder.build()

        val formData = getFormData(journeyData)

        assertTrue(formData.none { it.fieldHeading == "registerAsALandlord.checkAnswers.rowHeading.contactAddressOutsideUK" })
    }
}
