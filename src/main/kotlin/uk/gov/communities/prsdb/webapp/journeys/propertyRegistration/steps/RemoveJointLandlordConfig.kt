package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.ArrayIndexParameterService

// TODO PDJB-117: Implement RemoveJointLandlordStep
@JourneyFrameworkComponent
class RemoveJointLandlordConfig(
    private val urlParameterService: ArrayIndexParameterService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, JointLandlordsState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JointLandlordsState) =
        mapOf(
            "todoComment" to
                "Remove joint landlord page with index ${urlParameterService.getParameterOrNull()}",
        )

    override fun chooseTemplate(state: JointLandlordsState): String = "forms/todo"

    override fun mode(state: JointLandlordsState) = Complete.COMPLETE

    override fun afterStepDataIsAdded(state: JointLandlordsState) {
        val indexToRemove = urlParameterService.getParameterOrNull()

        if (indexToRemove == null || indexToRemove < 0 || indexToRemove >= state.invitedJointLandlords.size) {
            return
        }

        val currentList = state.invitedJointLandlords.toMutableList()
        currentList.removeAt(indexToRemove)
        state.invitedJointLandlordEmails = currentList
    }
}

@JourneyFrameworkComponent
final class RemoveJointLandlordStep(
    stepConfig: RemoveJointLandlordConfig,
) : RequestableStep<Complete, NoInputFormModel, JointLandlordsState>(stepConfig)
