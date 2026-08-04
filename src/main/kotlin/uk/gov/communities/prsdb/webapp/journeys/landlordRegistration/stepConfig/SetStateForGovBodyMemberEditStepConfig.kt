package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

@JourneyFrameworkComponent
class SetStateForGovBodyMemberEditStepConfig(
    private val collectionKeyParameterService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, OrgGovBodyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: OrgGovBodyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: OrgGovBodyState): String = ""

    override fun beforeAttemptingToReachStep(state: OrgGovBodyState): Boolean {
        val keyToEdit = collectionKeyParameterService.getParameterOrNull()
        val currentMap = state.governingBodyMembersMap ?: emptyMap()
        return keyToEdit != null && keyToEdit in currentMap.keys
    }

    override fun afterStepIsReached(state: OrgGovBodyState) {
        val keyToEdit = collectionKeyParameterService.getParameterOrNull() ?: return
        state.editingGovBodyMemberId = keyToEdit
    }

    override fun mode(state: OrgGovBodyState): Complete = Complete.COMPLETE
}

@JourneyFrameworkComponent
final class SetStateForGovBodyMemberEditStep(
    stepConfig: SetStateForGovBodyMemberEditStepConfig,
) : JourneyStep.RequestableStep<Complete, NoInputFormModel, OrgGovBodyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "load-governing-body-member-for-edit"
    }
}
