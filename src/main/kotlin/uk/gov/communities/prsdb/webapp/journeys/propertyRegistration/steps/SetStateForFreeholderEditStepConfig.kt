package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

@JourneyFrameworkComponent
class SetStateForFreeholderEditStepConfig(
    private val collectionKeyParameterService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, FreeholdersDetailsState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: FreeholdersDetailsState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: FreeholdersDetailsState): String = ""

    override fun beforeAttemptingToReachStep(state: FreeholdersDetailsState): Boolean {
        val keyToEdit = collectionKeyParameterService.getParameterOrNull()
        val currentMap = state.freeholdersMap ?: emptyMap()
        return keyToEdit != null && keyToEdit in currentMap.keys
    }

    override fun afterStepIsReached(state: FreeholdersDetailsState) {
        val keyToEdit = collectionKeyParameterService.getParameterOrNull() ?: return
        state.editingFreeholderId = keyToEdit
    }

    override fun mode(state: FreeholdersDetailsState): Complete = Complete.COMPLETE
}

@JourneyFrameworkComponent
final class SetStateForFreeholderEditStep(
    stepConfig: SetStateForFreeholderEditStepConfig,
) : JourneyStep.RequestableStep<Complete, NoInputFormModel, FreeholdersDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "load-freeholder-for-edit"
    }
}
