package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

// TODO PDJB-1979: replace this placeholder with the real "are you sure you want to remove this superior landlord" page
//  (content, input and validation). Rendered with the shared forms/todo placeholder page.
@JourneyFrameworkComponent
class ConfirmRemoveSuperiorLandlordStepConfig(
    private val collectionKeyParameterService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<AnyMembers, NoInputFormModel, SuperiorLandlordsDetailsState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: SuperiorLandlordsDetailsState): Map<String, Any?> =
        mapOf(
            "todoComment" to
                "TODO (PDJB-1979): Are you sure you want to remove this superior landlord? - content, input and validation to be added",
        )

    override fun chooseTemplate(state: SuperiorLandlordsDetailsState) = "forms/todo"

    override fun beforeAttemptingToReachStep(state: SuperiorLandlordsDetailsState): Boolean {
        val keyToRemove = collectionKeyParameterService.getParameterOrNull()
        val currentMap = state.superiorLandlordsMap ?: emptyMap()
        return keyToRemove != null && keyToRemove in currentMap.keys
    }

    // TODO PDJB-1979: only remove the superior landlord when the user confirms they want to remove them, as in
    //  RemoveJointLandlordAreYouSureStepConfig
    override fun afterStepDataIsAdded(state: SuperiorLandlordsDetailsState) {
        val keyToRemove = collectionKeyParameterService.getParameterOrNull() ?: return
        val currentMap = state.superiorLandlordsMap?.toMutableMap() ?: return
        currentMap.remove(keyToRemove)
        state.superiorLandlordsMap = currentMap
    }

    override fun mode(state: SuperiorLandlordsDetailsState): AnyMembers =
        if (state.superiorLandlordsMap.isNullOrEmpty()) AnyMembers.NO_MEMBERS else AnyMembers.SOME_MEMBERS
}

@JourneyFrameworkComponent
final class ConfirmRemoveSuperiorLandlordStep(
    stepConfig: ConfirmRemoveSuperiorLandlordStepConfig,
) : RequestableStep<AnyMembers, NoInputFormModel, SuperiorLandlordsDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "remove-superior-landlord"
    }
}
