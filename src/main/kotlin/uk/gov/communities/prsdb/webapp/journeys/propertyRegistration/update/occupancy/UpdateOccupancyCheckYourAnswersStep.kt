package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@JourneyFrameworkComponent
class UpdateOccupancyCheckYourAnswersStepConfig :
    AbstractCheckYourAnswersStepConfig<UpdateOccupancyJourneyState>() {
    // TODO(PDJB-1635): populate the real occupancy check-your-answers summary content. This is a skeleton page: it
    //  shows the warning and submit button with an empty summary list until the real content is built.
    override fun getStepSpecificContent(state: UpdateOccupancyJourneyState): Map<String, Any?> =
        mapOf(
            "title" to "propertyDetails.update.title",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "insetText" to true,
            "summaryName" to "forms.update.checkOccupancy.notOccupied.summaryName",
            "summaryListData" to emptyList<SummaryListRowViewModel>(),
        )

    override fun resolveNextDestination(
        state: UpdateOccupancyJourneyState,
        defaultDestination: Destination,
    ): Destination = defaultDestination
}

@JourneyFrameworkComponent
final class UpdateOccupancyCheckYourAnswersStep(
    stepConfig: UpdateOccupancyCheckYourAnswersStepConfig,
) : AbstractCheckYourAnswersStep<UpdateOccupancyJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-your-answers"
    }
}
