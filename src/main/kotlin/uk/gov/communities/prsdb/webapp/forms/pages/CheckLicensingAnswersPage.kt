package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getLicenceNumberUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getLicensingTypeUpdateIfPresent
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class CheckLicensingAnswersPage :
    AbstractPage(
        NoInputFormModel::class,
        "forms/checkAnswersForm",
        mapOf(
            "title" to "propertyDetails.update.title",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
        ),
    ) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {
        filteredJourneyData!!
        modelAndView.addObject("summaryName", getSummaryName(filteredJourneyData))
        modelAndView.addObject("formData", getFormData(filteredJourneyData))
    }

    private fun getSummaryName(journeyData: JourneyData) =
        if (journeyData.getLicensingTypeUpdateIfPresent()!! == LicensingType.NO_LICENSING) {
            "forms.update.checkLicensing.remove.summaryName"
        } else {
            "forms.update.checkLicensing.update.summaryName"
        }

    private fun getFormData(journeyData: JourneyData): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.propertyDetails.licensing",
                getLicensingSummaryValue(journeyData),
                UpdatePropertyDetailsStepId.UpdateLicensingType.urlPathSegment,
            ),
        )

    private fun getLicensingSummaryValue(journeyData: JourneyData): Any {
        val licensingType = journeyData.getLicensingTypeUpdateIfPresent()!!
        return if (licensingType == LicensingType.NO_LICENSING) {
            licensingType
        } else {
            listOf(licensingType, journeyData.getLicenceNumberUpdateIfPresent()!!)
        }
    }
}
