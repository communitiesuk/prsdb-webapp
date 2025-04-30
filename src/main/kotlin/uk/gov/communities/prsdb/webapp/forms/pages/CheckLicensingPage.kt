package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.helpers.PropertyRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class CheckLicensingPage :
    AbstractPage(
        NoInputFormModel::class,
        "forms/checkAnswersForm",
        mapOf(
            "title" to "registerProperty.title",
            "submitButtonText" to "forms.buttons.confirm",
            "summaryName" to "registerLaUser.checkAnswers.summaryName",
        ),
        shouldDisplaySectionHeader = false,
    ) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {
        filteredJourneyData!!
        val licensingType = PropertyRegistrationJourneyDataHelper.getLicensingType(filteredJourneyData)!!
        val licenceNumber = PropertyRegistrationJourneyDataHelper.getLicenseNumber(filteredJourneyData)!!
        val licensingSummaryValue = getLicensingSummaryValue(licenceNumber, licensingType)

        val formData =
            listOf(
                SummaryListRowViewModel(
                    "forms.checkPropertyAnswers.propertyDetails.licensing",
                    licensingSummaryValue,
                    RegisterPropertyStepId.LicensingType.urlPathSegment,
                ),
            )

        modelAndView.addObject("formData", formData)
    }

    private fun getLicensingSummaryValue(
        licenceNumber: String?,
        licensingType: LicensingType,
    ): Any =
        if (licensingType != LicensingType.NO_LICENSING) {
            listOf(licensingType, licenceNumber)
        } else {
            licensingType
        }
}
