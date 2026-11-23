package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.superiorLandlords

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@JourneyFrameworkComponent
class UpdateSuperiorLandlordsCheckYourAnswersStepConfig : AbstractCheckYourAnswersStepConfig<UpdateSuperiorLandlordsJourneyState>() {
    override fun getStepSpecificContent(state: UpdateSuperiorLandlordsJourneyState): Map<String, Any?> =
        mapOf(
            "title" to "forms.update.checkSuperiorLandlords.pageTitle",
            "summaryName" to "forms.update.checkSuperiorLandlords.summaryName",
            "summaryListData" to
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "registerProperty.superiorLandlords.checkAnswers.anyoneInOwnershipChain",
                        "commonText.no",
                        Destination(state.superiorLandlordsTask.anyoneInOwnershipChainStep),
                    ),
                ),
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
        )

    // The complete step deletes the journey once the update has been saved
    override fun resolveNextDestination(
        state: UpdateSuperiorLandlordsJourneyState,
        defaultDestination: Destination,
    ): Destination = defaultDestination
}

@JourneyFrameworkComponent
final class UpdateSuperiorLandlordsCheckYourAnswersStep(
    stepConfig: UpdateSuperiorLandlordsCheckYourAnswersStepConfig,
) : AbstractCheckYourAnswersStep<UpdateSuperiorLandlordsJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-your-answers"
    }
}
