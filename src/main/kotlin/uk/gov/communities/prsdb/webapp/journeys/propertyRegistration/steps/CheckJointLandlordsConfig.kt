package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-114: Implement CheckJointLandlordsStep
@JourneyFrameworkComponent
class CheckJointLandlordsConfig : AbstractGenericStepConfig<Complete, NoInputFormModel, JointLandlordsState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JointLandlordsState) =
        mapOf("todoComment" to "TODO PDJB-114: Implement check joint landlords page")

    override fun chooseTemplate(state: JointLandlordsState): String = "forms/todo"

    override fun mode(state: JointLandlordsState) = Complete.COMPLETE
}

@JourneyFrameworkComponent
final class CheckJointLandlordsStep(
    stepConfig: CheckJointLandlordsConfig,
) : RequestableStep<Complete, NoInputFormModel, JointLandlordsState>(stepConfig)
