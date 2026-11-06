package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

@JourneyFrameworkComponent
class SetStateForSuperiorLandlordEditStepConfig(
    private val collectionKeyParameterService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, SuperiorLandlordsDetailsState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: SuperiorLandlordsDetailsState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: SuperiorLandlordsDetailsState): String = ""

    override fun beforeAttemptingToReachStep(state: SuperiorLandlordsDetailsState): Boolean {
        val keyToEdit = collectionKeyParameterService.getParameterOrNull()
        val currentMap = state.superiorLandlordsMap ?: emptyMap()
        return keyToEdit != null && keyToEdit in currentMap.keys
    }

    override fun afterStepIsReached(state: SuperiorLandlordsDetailsState) {
        val keyToEdit = collectionKeyParameterService.getParameterOrNull() ?: return
        state.editingSuperiorLandlordId = keyToEdit
    }

    override fun mode(state: SuperiorLandlordsDetailsState): Complete = Complete.COMPLETE
}

@JourneyFrameworkComponent
final class SetStateForSuperiorLandlordEditStep(
    stepConfig: SetStateForSuperiorLandlordEditStepConfig,
) : JourneyStep.RequestableStep<Complete, NoInputFormModel, SuperiorLandlordsDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "load-superior-landlord-for-edit"
    }
}
