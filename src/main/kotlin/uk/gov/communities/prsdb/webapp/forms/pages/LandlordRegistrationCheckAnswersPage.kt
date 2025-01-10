package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.helpers.LandlordJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.FormSummaryViewModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService

class LandlordRegistrationCheckAnswersPage(
    private val addressDataService: AddressDataService,
) : Page(
        formModel = CheckAnswersFormModel::class,
        templateName = "forms/checkAnswersForm",
        content =
            mapOf(
                "title" to "registerAsALandlord.title",
                "summaryName" to "registerAsALandlord.checkAnswers.summaryName",
                "submitButtonText" to "forms.buttons.confirmAndContinue",
            ),
    ) {
    override fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: Map<String, Any?>?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
    ): String {
        journeyData!!

        val formData =
            getIdentityFormData(journeyData) +
                getEmailAndPhoneFormData(journeyData) +
                getAddressFormData(journeyData)

        model.addAttribute("formData", formData)
        return super.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl)
    }

    private fun getIdentityFormData(journeyData: JourneyData): List<FormSummaryViewModel> {
        val isIdentityVerified = LandlordJourneyDataHelper.isIdentityVerified(journeyData)

        return listOf(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                LandlordJourneyDataHelper.getName(journeyData)!!,
                if (isIdentityVerified) null else LandlordRegistrationStepId.Name.urlPathSegment,
            ),
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                LandlordJourneyDataHelper.getDOB(journeyData)!!,
                if (isIdentityVerified) null else LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
            ),
        )
    }

    private fun getEmailAndPhoneFormData(journeyData: JourneyData): List<FormSummaryViewModel> =
        listOf(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.email",
                LandlordJourneyDataHelper.getEmail(journeyData)!!,
                LandlordRegistrationStepId.Email.urlPathSegment,
            ),
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber",
                LandlordJourneyDataHelper.getPhoneNumber(journeyData)!!,
                LandlordRegistrationStepId.PhoneNumber.urlPathSegment,
            ),
        )

    private fun getAddressFormData(journeyData: JourneyData): List<FormSummaryViewModel> {
        val livesInUK = LandlordJourneyDataHelper.getLivesInUK(journeyData)!!

        return getLivesInUKFormData(livesInUK) +
            (if (!livesInUK) getInternationalAddressFormData(journeyData) else emptyList()) +
            getContactAddressFormData(journeyData, addressDataService, livesInUK)
    }

    private fun getLivesInUKFormData(livesInUK: Boolean): List<FormSummaryViewModel> =
        listOf(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.ukResident",
                livesInUK,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
            ),
        )

    private fun getInternationalAddressFormData(journeyData: JourneyData): List<FormSummaryViewModel> =
        listOf(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.countryOfResidence",
                LandlordJourneyDataHelper.getNonUKCountryOfResidence(journeyData)!!,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
            ),
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.contactAddressOutsideUK",
                LandlordJourneyDataHelper.getInternationalAddress(journeyData)!!,
                LandlordRegistrationStepId.InternationalAddress.urlPathSegment,
            ),
        )

    private fun getContactAddressFormData(
        journeyData: JourneyData,
        addressDataService: AddressDataService,
        livesInUK: Boolean,
    ): FormSummaryViewModel =
        FormSummaryViewModel(
            if (livesInUK) {
                "registerAsALandlord.checkAnswers.rowHeading.contactAddress"
            } else {
                "registerAsALandlord.checkAnswers.rowHeading.ukContactAddress"
            },
            LandlordJourneyDataHelper.getAddress(journeyData, addressDataService)!!.singleLineAddress,
            getContactAddressChangeURLPathSegment(journeyData, livesInUK),
        )

    private fun getContactAddressChangeURLPathSegment(
        journeyData: JourneyData,
        livesInUK: Boolean,
    ): String =
        if (livesInUK) {
            if (LandlordJourneyDataHelper.isManualAddressChosen(journeyData)) {
                LandlordRegistrationStepId.ManualAddress.urlPathSegment
            } else {
                LandlordRegistrationStepId.LookupAddress.urlPathSegment
            }
        } else {
            val isContactAddress = true
            if (LandlordJourneyDataHelper.isManualAddressChosen(journeyData, isContactAddress)
            ) {
                LandlordRegistrationStepId.ManualContactAddress.urlPathSegment
            } else {
                LandlordRegistrationStepId.LookupContactAddress.urlPathSegment
            }
        }
}
