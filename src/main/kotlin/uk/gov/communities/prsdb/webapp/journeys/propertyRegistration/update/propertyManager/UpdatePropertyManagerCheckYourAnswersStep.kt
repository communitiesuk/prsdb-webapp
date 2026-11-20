package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.propertyManager

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@JourneyFrameworkComponent
class UpdatePropertyManagerCheckYourAnswersStepConfig : AbstractCheckYourAnswersStepConfig<UpdatePropertyManagerJourneyState>() {
    override fun getStepSpecificContent(state: UpdatePropertyManagerJourneyState): Map<String, Any?> =
        mapOf(
            "title" to "forms.update.checkPropertyManager.pageTitle",
            "summaryName" to "forms.update.checkPropertyManager.summaryName",
            "summaryListData" to
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "registerProperty.propertyManager.usingPropertyManager.fieldSetHeading",
                        "registerProperty.propertyManager.usingPropertyManager.radios.no",
                        Destination(state.propertyManagerTask.propertyManagerStep),
                    ),
                ),
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
        )

    // The complete step deletes the journey once the update has been saved
    override fun resolveNextDestination(
        state: UpdatePropertyManagerJourneyState,
        defaultDestination: Destination,
    ): Destination = defaultDestination
}

@JourneyFrameworkComponent
final class UpdatePropertyManagerCheckYourAnswersStep(
    stepConfig: UpdatePropertyManagerCheckYourAnswersStepConfig,
) : AbstractCheckYourAnswersStep<UpdatePropertyManagerJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-your-answers"
    }
}
