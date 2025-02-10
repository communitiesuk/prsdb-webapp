package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataExtensions.getAddress
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataExtensions.getDOB
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataExtensions.getEmail
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataExtensions.getInternationalAddress
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataExtensions.getLivesInUK
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataExtensions.getName
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataExtensions.getNonUKCountryOfResidence
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataExtensions.getPhoneNumber
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataExtensions.isIdentityVerified
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataExtensions.isManualAddressChosen
import uk.gov.communities.prsdb.webapp.models.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.SummaryListRowViewModel
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

    private fun getIdentityFormData(journeyData: JourneyData): List<SummaryListRowViewModel> {
        val isIdentityVerified = journeyData.isIdentityVerified()

        return listOf(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                journeyData.getName()!!,
                if (isIdentityVerified) null else LandlordRegistrationStepId.Name.urlPathSegment,
            ),
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                journeyData.getDOB()!!,
                if (isIdentityVerified) null else LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
            ),
        )
    }

    private fun getEmailAndPhoneFormData(journeyData: JourneyData): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.email",
                journeyData.getEmail()!!,
                LandlordRegistrationStepId.Email.urlPathSegment,
            ),
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber",
                journeyData.getPhoneNumber()!!,
                LandlordRegistrationStepId.PhoneNumber.urlPathSegment,
            ),
        )

    private fun getAddressFormData(journeyData: JourneyData): List<SummaryListRowViewModel> {
        val livesInUK = journeyData.getLivesInUK()!!

        return getLivesInUKFormData(livesInUK) +
            (if (!livesInUK) getInternationalAddressFormData(journeyData) else emptyList()) +
            getContactAddressFormData(journeyData, addressDataService, livesInUK)
    }

    private fun getLivesInUKFormData(livesInUK: Boolean): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.ukResident",
                livesInUK,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
            ),
        )

    private fun getInternationalAddressFormData(journeyData: JourneyData): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.countryOfResidence",
                journeyData.getNonUKCountryOfResidence()!!,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
            ),
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.contactAddressOutsideUK",
                journeyData.getInternationalAddress()!!,
                LandlordRegistrationStepId.InternationalAddress.urlPathSegment,
            ),
        )

    private fun getContactAddressFormData(
        journeyData: JourneyData,
        addressDataService: AddressDataService,
        livesInUK: Boolean,
    ): SummaryListRowViewModel =
        SummaryListRowViewModel(
            if (livesInUK) {
                "registerAsALandlord.checkAnswers.rowHeading.contactAddress"
            } else {
                "registerAsALandlord.checkAnswers.rowHeading.ukContactAddress"
            },
            journeyData.getAddress(addressDataService)!!.singleLineAddress,
            getContactAddressChangeURLPathSegment(journeyData, livesInUK),
        )

    private fun getContactAddressChangeURLPathSegment(
        journeyData: JourneyData,
        livesInUK: Boolean,
    ): String =
        if (livesInUK) {
            if (journeyData.isManualAddressChosen()) {
                LandlordRegistrationStepId.ManualAddress.urlPathSegment
            } else {
                LandlordRegistrationStepId.LookupAddress.urlPathSegment
            }
        } else {
            val isContactAddress = true
            if (journeyData.isManualAddressChosen(isContactAddress)
            ) {
                LandlordRegistrationStepId.ManualContactAddress.urlPathSegment
            } else {
                LandlordRegistrationStepId.LookupContactAddress.urlPathSegment
            }
        }
}
