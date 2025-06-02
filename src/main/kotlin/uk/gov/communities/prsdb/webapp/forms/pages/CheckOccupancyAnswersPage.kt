package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getIsOccupiedUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getNumberOfHouseholdsUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getNumberOfPeopleUpdateIfPresent
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class CheckOccupancyAnswersPage(
    private val numberOfHouseholdsStepId: UpdatePropertyDetailsStepId,
    private val numberOfPeopleStepId: UpdatePropertyDetailsStepId,
) : AbstractPage(
        NoInputFormModel::class,
        "forms/checkAnswersForm",
        mapOf(
            "title" to "propertyDetails.update.title",
            "summaryName" to "forms.update.checkOccupancy.summaryName",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
        ),
    ) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {
        filteredJourneyData!!
        modelAndView.addObject("formData", getFormData(filteredJourneyData))
    }

    private fun getFormData(filteredJourneyData: JourneyData) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val isOccupied = filteredJourneyData.getIsOccupiedUpdateIfPresent()!!
                add(occupancyStatusRow(isOccupied))
                if (isOccupied) addAll(tenancyRows(filteredJourneyData))
            }.toList()

    private fun occupancyStatusRow(isOccupied: Boolean) =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.occupancy.fieldSetHeading",
            isOccupied,
            UpdatePropertyDetailsStepId.UpdateOccupancy.urlPathSegment,
        )

    private fun tenancyRows(filteredJourneyData: JourneyData): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.numberOfHouseholds.fieldSetHeading",
                filteredJourneyData.getNumberOfHouseholdsUpdateIfPresent(numberOfHouseholdsStepId)!!,
                numberOfHouseholdsStepId.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.numberOfPeople.fieldSetHeading",
                filteredJourneyData.getNumberOfPeopleUpdateIfPresent(numberOfPeopleStepId)!!,
                numberOfPeopleStepId.urlPathSegment,
            ),
        )
}
