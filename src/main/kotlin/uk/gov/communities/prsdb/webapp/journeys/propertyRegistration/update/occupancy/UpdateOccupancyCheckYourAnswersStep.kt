package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig

@JourneyFrameworkComponent
class UpdateOccupancyCheckYourAnswersStepConfig(
    private val occupancyDetailsHelper: OccupancyDetailsHelper,
) : AbstractCheckYourAnswersStepConfig<UpdateOccupancyJourneyState>() {
    override fun getStepSpecificContent(state: UpdateOccupancyJourneyState): Map<String, Any?> =
        mapOf(
            "title" to "forms.checkAnswers.heading",
            "insetText" to true,
            "summaryName" to "forms.update.checkOccupancy.notOccupied.summaryName",
            "summaryListData" to occupancyDetailsHelper.getOccupancyStatusSummaryList(state),
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
        )

    // override this as the next step (CompleteOccupancyUpdateStep) will handle deleting the journey and saving the update
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
