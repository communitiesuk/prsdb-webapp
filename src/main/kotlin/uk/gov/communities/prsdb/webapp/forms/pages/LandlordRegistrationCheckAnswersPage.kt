package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class LandlordRegistrationCheckAnswersPage :
    AbstractPage(
        formModel = CheckAnswersFormModel::class,
        templateName = "forms/checkAnswersForm",
        content =
            mapOf(
                "title" to "registerAsALandlord.title",
                "summaryName" to "registerAsALandlord.checkAnswers.summaryName",
                "submitButtonText" to "forms.buttons.confirmAndContinue",
            ),
        shouldDisplaySectionHeader = true,
    ) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {
        filteredJourneyData!!

        val formData =
            getIdentityFormData(filteredJourneyData) +
                getEmailAndPhoneFormData(filteredJourneyData) +
                getAddressFormData(filteredJourneyData)

        modelAndView.addObject("formData", formData)
    }

    private fun getIdentityFormData(journeyData: JourneyData): List<SummaryListRowViewModel> {
        val isIdentityVerified = LandlordRegistrationJourneyDataHelper.isIdentityVerified(journeyData)

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                LandlordRegistrationJourneyDataHelper.getName(journeyData)!!,
                if (isIdentityVerified) null else LandlordRegistrationStepId.Name.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                LandlordRegistrationJourneyDataHelper.getDOB(journeyData)!!,
                if (isIdentityVerified) null else LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
            ),
        )
    }

    private fun getEmailAndPhoneFormData(journeyData: JourneyData): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.email",
                LandlordRegistrationJourneyDataHelper.getEmail(journeyData)!!,
                LandlordRegistrationStepId.Email.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber",
                LandlordRegistrationJourneyDataHelper.getPhoneNumber(journeyData)!!,
                LandlordRegistrationStepId.PhoneNumber.urlPathSegment,
            ),
        )

    private fun getAddressFormData(journeyData: JourneyData): List<SummaryListRowViewModel> {
        val livesInEnglandOrWales = LandlordRegistrationJourneyDataHelper.getLivesInEnglandOrWales(journeyData)!!

        return getLivesInEnglandOrWalesFormData(livesInEnglandOrWales) +
            (if (!livesInEnglandOrWales) getNonEnglandOrWalesAddressFormData(journeyData) else emptyList()) +
            getContactAddressFormData(journeyData, livesInEnglandOrWales)
    }

    private fun getLivesInEnglandOrWalesFormData(livesInEnglandOrWales: Boolean): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.englandOrWalesResident",
                livesInEnglandOrWales,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
            ),
        )

    private fun getNonEnglandOrWalesAddressFormData(journeyData: JourneyData): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.countryOfResidence",
                LandlordRegistrationJourneyDataHelper.getNonEnglandOrWalesCountryOfResidence(journeyData)!!,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.nonEnglandOrWalesContactAddress",
                LandlordRegistrationJourneyDataHelper.getNonEnglandOrWalesAddress(journeyData)!!,
                LandlordRegistrationStepId.NonEnglandOrWalesAddress.urlPathSegment,
            ),
        )

    private fun getContactAddressFormData(
        journeyData: JourneyData,
        livesInEnglandOrWales: Boolean,
    ): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            if (livesInEnglandOrWales) {
                "registerAsALandlord.checkAnswers.rowHeading.contactAddress"
            } else {
                "registerAsALandlord.checkAnswers.rowHeading.englandOrWalesContactAddress"
            },
            LandlordRegistrationJourneyDataHelper.getAddress(journeyData)!!.singleLineAddress,
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
            if (LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData, isContactAddress)) {
                LandlordRegistrationStepId.ManualContactAddress.urlPathSegment
            } else {
                LandlordRegistrationStepId.LookupContactAddress.urlPathSegment
            }
        }
}
