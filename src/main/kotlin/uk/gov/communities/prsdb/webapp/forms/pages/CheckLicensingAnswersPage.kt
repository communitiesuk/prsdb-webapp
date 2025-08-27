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
    missingAnswersRedirect: String,
) : BasicCheckAnswersPage(
        content =
            mapOf(
                "title" to "propertyDetails.update.title",
                "showWarning" to true,
                "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                "insetText" to "forms.update.checkOccupancy.insetText",
            ),
        journeyDataService = journeyDataService,
        missingAnswersRedirect = missingAnswersRedirect,
    ) {
    override fun getSummaryList(filteredJourneyData: JourneyData): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val licensingType = filteredJourneyData.getLicensingTypeUpdateIfPresent()!!
                add(licensingTypeRow(licensingType))
                if (licensingType != LicensingType.NO_LICENSING) add(licensingNumberRow(filteredJourneyData))
            }

    override fun addExtraContentToModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData,
    ) {
        modelAndView.addObject("summaryName", getSummaryName(filteredJourneyData))
    }

    private fun licensingTypeRow(licensingType: LicensingType) =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.propertyDetails.licensingType",
            licensingType,
            UpdatePropertyDetailsStepId.UpdateLicensingType.urlPathSegment,
        )

    private fun licensingNumberRow(filteredJourneyData: JourneyData) =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.propertyDetails.licensingNumber",
            filteredJourneyData.getLicenceNumberUpdateIfPresent()!!,
            null,
        )

    private fun getSummaryName(filteredJourneyData: JourneyData) =
        if (filteredJourneyData.getLicensingTypeUpdateIfPresent()!! == LicensingType.NO_LICENSING) {
            "forms.update.checkLicensing.remove.summaryName"
        } else {
            "forms.update.checkLicensing.update.summaryName"
        }
}
