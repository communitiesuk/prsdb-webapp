package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

// TODO PDJB-1970: replace this placeholder with the real "are you sure you want to remove this freeholder" page
//  (content, input and validation). Rendered with the shared forms/todo placeholder page.
@JourneyFrameworkComponent
class ConfirmRemoveFreeholderStepConfig(
    private val collectionKeyParameterService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<AnyMembers, NoInputFormModel, FreeholdersDetailsState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: FreeholdersDetailsState): Map<String, Any?> =
        mapOf(
            "todoComment" to
                "TODO (PDJB-1970): Are you sure you want to remove this freeholder? - content, input and validation to be added",
        )

    override fun chooseTemplate(state: FreeholdersDetailsState) = "forms/todo"

    override fun beforeAttemptingToReachStep(state: FreeholdersDetailsState): Boolean {
        val keyToRemove = collectionKeyParameterService.getParameterOrNull()
        val currentMap = state.freeholdersMap ?: emptyMap()
        return keyToRemove != null && keyToRemove in currentMap.keys && (currentMap.size > 1 || state.allowRemovingLastMember)
    }

    // TODO PDJB-1970: only remove the freeholder when the user confirms they want to remove them, as in
    //  RemoveJointLandlordAreYouSureStepConfig
    override fun afterStepDataIsAdded(state: FreeholdersDetailsState) {
        val keyToRemove = collectionKeyParameterService.getParameterOrNull() ?: return
        val currentMap = state.freeholdersMap?.toMutableMap() ?: return
        currentMap.remove(keyToRemove)
        state.freeholdersMap = currentMap
    }

    override fun mode(state: FreeholdersDetailsState): AnyMembers =
        if (state.freeholdersMap.isNullOrEmpty()) AnyMembers.NO_MEMBERS else AnyMembers.SOME_MEMBERS
}

@JourneyFrameworkComponent
final class ConfirmRemoveFreeholderStep(
    stepConfig: ConfirmRemoveFreeholderStepConfig,
) : RequestableStep<AnyMembers, NoInputFormModel, FreeholdersDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "remove-freeholder"
    }
}
