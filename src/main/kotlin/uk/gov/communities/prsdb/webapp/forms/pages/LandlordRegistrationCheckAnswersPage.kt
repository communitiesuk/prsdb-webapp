package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService

class LandlordRegistrationCheckAnswersPage(
    private val addressDataService: AddressDataService,
    displaySectionHeader: Boolean = false,
) : Page(
        formModel = CheckAnswersFormModel::class,
        templateName = "forms/checkAnswersForm",
        content =
            mapOf(
                "title" to "registerAsALandlord.title",
                "summaryName" to "registerAsALandlord.checkAnswers.summaryName",
                "submitButtonText" to "forms.buttons.confirmAndContinue",
            ),
        shouldDisplaySectionHeader = displaySectionHeader,
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
        val isIdentityVerified = LandlordRegistrationJourneyDataHelper.isIdentityVerified(journeyData)

        return listOf(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                LandlordRegistrationJourneyDataHelper.getName(journeyData)!!,
                if (isIdentityVerified) null else LandlordRegistrationStepId.Name.urlPathSegment,
            ),
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                LandlordRegistrationJourneyDataHelper.getDOB(journeyData)!!,
                if (isIdentityVerified) null else LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
            ),
        )
    }

    private fun getEmailAndPhoneFormData(journeyData: JourneyData): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.email",
                LandlordRegistrationJourneyDataHelper.getEmail(journeyData)!!,
                LandlordRegistrationStepId.Email.urlPathSegment,
            ),
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber",
                LandlordRegistrationJourneyDataHelper.getPhoneNumber(journeyData)!!,
                LandlordRegistrationStepId.PhoneNumber.urlPathSegment,
            ),
        )

    private fun getAddressFormData(journeyData: JourneyData): List<SummaryListRowViewModel> {
        val livesInEnglandOrWales = LandlordRegistrationJourneyDataHelper.getLivesInEnglandOrWales(journeyData)!!

        return getLivesInEnglandOrWalesFormData(livesInEnglandOrWales) +
            (if (!livesInEnglandOrWales) getNonEnglandOrWalesAddressFormData(journeyData) else emptyList()) +
            getContactAddressFormData(journeyData, addressDataService, livesInEnglandOrWales)
    }

    private fun getLivesInEnglandOrWalesFormData(livesInEnglandOrWales: Boolean): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.englandOrWalesResident",
                livesInEnglandOrWales,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
            ),
        )

    private fun getNonEnglandOrWalesAddressFormData(journeyData: JourneyData): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.countryOfResidence",
                LandlordRegistrationJourneyDataHelper.getNonEnglandOrWalesCountryOfResidence(journeyData)!!,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
            ),
            SummaryListRowViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.nonEnglandOrWalesContactAddress",
                LandlordRegistrationJourneyDataHelper.getNonEnglandOrWalesAddress(journeyData)!!,
                LandlordRegistrationStepId.NonEnglandOrWalesAddress.urlPathSegment,
            ),
        )

    private fun getContactAddressFormData(
        journeyData: JourneyData,
        addressDataService: AddressDataService,
        livesInEnglandOrWales: Boolean,
    ): SummaryListRowViewModel =
        SummaryListRowViewModel(
            if (livesInEnglandOrWales) {
                "registerAsALandlord.checkAnswers.rowHeading.contactAddress"
            } else {
                "registerAsALandlord.checkAnswers.rowHeading.englandOrWalesContactAddress"
            },
            LandlordRegistrationJourneyDataHelper.getAddress(journeyData, addressDataService)!!.singleLineAddress,
            getContactAddressChangeURLPathSegment(journeyData, livesInEnglandOrWales),
        )

    private fun getContactAddressChangeURLPathSegment(
        journeyData: JourneyData,
        livesInEnglandOrWales: Boolean,
    ): String =
        if (livesInEnglandOrWales) {
            if (LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData)) {
                LandlordRegistrationStepId.ManualAddress.urlPathSegment
            } else {
                LandlordRegistrationStepId.LookupAddress.urlPathSegment
            }
        } else {
            val isContactAddress = true
            if (LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData, isContactAddress)
            ) {
                LandlordRegistrationStepId.ManualContactAddress.urlPathSegment
            } else {
                LandlordRegistrationStepId.LookupContactAddress.urlPathSegment
            }
        }
}
