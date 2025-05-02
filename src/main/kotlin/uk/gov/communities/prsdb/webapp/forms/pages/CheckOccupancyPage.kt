package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.PropertyRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

// TODO PRSD-1109 - the functions here have been copy and pasted from the property registration journey. Logic should be
// customised or commonised.
class CheckOccupancyPage :
    AbstractPage(
        NoInputFormModel::class,
        "forms/checkAnswersForm",
        mapOf(
            "title" to "registerProperty.title",
            "submitButtonText" to "forms.buttons.confirm",
            "summaryName" to "registerLaUser.checkAnswers.summaryName",
        ),
    ) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {
        filteredJourneyData!!
        val formData = getTenancyDetails(filteredJourneyData)

        modelAndView.addObject("formData", formData)
    }

    private fun getTenancyDetails(journeyData: JourneyData): List<SummaryListRowViewModel> {
        val occupied = PropertyRegistrationJourneyDataHelper.getIsOccupied(journeyData)!!
        return if (occupied) {
            getOccupyingTenantsDetails(journeyData)
        } else {
            listOf(
                SummaryListRowViewModel(
                    "forms.checkPropertyAnswers.propertyDetails.occupied",
                    false,
                    UpdatePropertyDetailsStepId.UpdateOccupancy.urlPathSegment,
                ),
            )
        }
    }

    private fun getOccupyingTenantsDetails(journeyData: JourneyData): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.occupied",
                true,
                UpdatePropertyDetailsStepId.UpdateOccupancy.urlPathSegment,
            ),
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.households",
                PropertyRegistrationJourneyDataHelper.getNumberOfHouseholds(journeyData),
                UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds.urlPathSegment,
            ),
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.people",
                PropertyRegistrationJourneyDataHelper.getNumberOfTenants(journeyData),
                UpdatePropertyDetailsStepId.UpdateNumberOfPeople.urlPathSegment,
            ),
        )
}
