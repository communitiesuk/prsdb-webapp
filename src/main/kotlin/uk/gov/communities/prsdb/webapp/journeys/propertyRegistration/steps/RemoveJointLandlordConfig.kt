package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.AnyLandlordsInvited
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

// TODO PDJB-117: Implement RemoveJointLandlordStep
@JourneyFrameworkComponent
class RemoveJointLandlordConfig(
    private val urlParameterService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<AnyLandlordsInvited, NoInputFormModel, JointLandlordsState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JointLandlordsState) =
        mapOf(
            "todoComment" to
                "Remove joint landlord page with index ${urlParameterService.getParameterOrNull()}",
        )

    override fun chooseTemplate(state: JointLandlordsState): String = "forms/todo"

    override fun mode(state: JointLandlordsState) =
        if (state.invitedJointLandlords.isEmpty()) {
            AnyLandlordsInvited.NO_LANDLORDS
        } else {
            AnyLandlordsInvited.SOME_LANDLORDS
        }

    override fun beforeAttemptingToReachStep(state: JointLandlordsState): Boolean {
        val keyToRemove = urlParameterService.getParameterOrNull()
        val currentMap = state.invitedJointLandlordEmailsMap ?: emptyMap()

        return keyToRemove != null && keyToRemove in currentMap.keys
    }

    override fun afterStepDataIsAdded(state: JointLandlordsState) {
        val currentMap = state.invitedJointLandlordEmailsMap?.toMutableMap() ?: mutableMapOf()

        currentMap.remove(urlParameterService.getParameterOrNull())
        state.invitedJointLandlordEmailsMap = currentMap
    }
}

@JourneyFrameworkComponent
final class RemoveJointLandlordStep(
    stepConfig: RemoveJointLandlordConfig,
) : RequestableStep<AnyLandlordsInvited, NoInputFormModel, JointLandlordsState>(stepConfig)
