package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getLicenceNumberUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getLicensingTypeUpdateIfPresent
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class CheckLicensingAnswersPage(
    journeyDataService: JourneyDataService,
) : CheckAnswersPage(
        content =
            mapOf(
                "title" to "propertyDetails.update.title",
                "showWarning" to true,
                "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            ),
        journeyDataService = journeyDataService,
    ) {
    override fun furtherEnrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData,
    ) {
        modelAndView.addObject("summaryName", getSummaryName(filteredJourneyData))
        modelAndView.addObject("summaryListData", getSummaryList(filteredJourneyData))
    }

    private fun getSummaryName(filteredJourneyData: JourneyData) =
        if (filteredJourneyData.getLicensingTypeUpdateIfPresent()!! == LicensingType.NO_LICENSING) {
            "forms.update.checkLicensing.remove.summaryName"
        } else {
            "forms.update.checkLicensing.update.summaryName"
        }

    private fun getSummaryList(filteredJourneyData: JourneyData): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.propertyDetails.licensing",
                getLicensingSummaryValue(filteredJourneyData),
                UpdatePropertyDetailsStepId.UpdateLicensingType.urlPathSegment,
            ),
        )

    private fun getLicensingSummaryValue(filteredJourneyData: JourneyData): Any {
        val licensingType = filteredJourneyData.getLicensingTypeUpdateIfPresent()!!
        return if (licensingType == LicensingType.NO_LICENSING) {
            licensingType
        } else {
            listOf(licensingType, filteredJourneyData.getLicenceNumberUpdateIfPresent()!!)
        }
    }
}
