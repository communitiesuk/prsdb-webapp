package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownership

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OwnershipStructureDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig

@JourneyFrameworkComponent
class UpdateOwnershipCheckYourAnswersStepConfig(
    private val ownershipStructureDetailsHelper: OwnershipStructureDetailsHelper,
) : AbstractCheckYourAnswersStepConfig<UpdateOwnershipJourneyState>() {
    override fun chooseTemplate(state: UpdateOwnershipJourneyState): String = "forms/updateOwnershipCheckAnswersForm"

    override fun getStepSpecificContent(state: UpdateOwnershipJourneyState): Map<String, Any?> =
        ownershipStructureDetailsHelper.getOwnershipStructureCyaContent(state, state.ownershipStructureTask) +
            mapOf(
                "insetTextKey" to "forms.update.checkOwnership.insetText",
                "warningTextKey" to "forms.warning",
                "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            )

    // override this as the next step will handle deleting the journey
    override fun resolveNextDestination(
        state: UpdateOwnershipJourneyState,
        defaultDestination: Destination,
    ): Destination = defaultDestination
}

@JourneyFrameworkComponent
final class UpdateOwnershipCheckYourAnswersStep(
    stepConfig: UpdateOwnershipCheckYourAnswersStepConfig,
) : AbstractCheckYourAnswersStep<UpdateOwnershipJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-your-answers"
    }
}
