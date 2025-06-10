package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsGroupIdentifier
import uk.gov.communities.prsdb.webapp.forms.steps.factories.PropertyDetailsUpdateJourneyStepFactory
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getIsOccupiedUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getNumberOfHouseholdsUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getNumberOfPeopleUpdateIfPresent
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class CheckOccupancyAnswersPage(
    private val stepGroupId: UpdatePropertyDetailsGroupIdentifier,
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
                val isOccupied = filteredJourneyData.getIsOccupiedUpdateIfPresent(stepGroupId)!!
                add(occupancyStatusRow(isOccupied))
                if (isOccupied) addAll(tenancyRows(filteredJourneyData))
            }.toList()

    private fun occupancyStatusRow(isOccupied: Boolean) =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.occupancy.fieldSetHeading",
            isOccupied,
            PropertyDetailsUpdateJourneyStepFactory.getOccupancyStepIdFor(stepGroupId).urlPathSegment,
        )

    private fun tenancyRows(filteredJourneyData: JourneyData): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.numberOfHouseholds.fieldSetHeading",
                filteredJourneyData.getNumberOfHouseholdsUpdateIfPresent(stepGroupId)!!,
                PropertyDetailsUpdateJourneyStepFactory.getNumberOfHouseholdsStepIdFor(stepGroupId).urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.numberOfPeople.fieldSetHeading",
                filteredJourneyData.getNumberOfPeopleUpdateIfPresent(stepGroupId)!!,
                PropertyDetailsUpdateJourneyStepFactory.getNumberOfPeopleStepIdFor(stepGroupId).urlPathSegment,
            ),
        )
}
