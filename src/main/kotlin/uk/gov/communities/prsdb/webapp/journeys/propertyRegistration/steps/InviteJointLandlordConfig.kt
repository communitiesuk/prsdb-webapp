package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.InviteJointLandlordsFormModel

// TODO PDJB-113: Implement InviteJointLandlordStep
@JourneyFrameworkComponent
class InviteJointLandlordConfig : AbstractRequestableStepConfig<Complete, InviteJointLandlordsFormModel, JointLandlordsState>() {
    override val formModelClass = InviteJointLandlordsFormModel::class

    override fun getStepSpecificContent(state: JointLandlordsState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "jointLandlords.inviteJointLandlord.fieldSetHeading",
            "label" to "jointLandlords.inviteJointLandlord.email.label",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: JointLandlordsState): String = "forms/emailForm"

    override fun mode(state: JointLandlordsState) = Complete.COMPLETE
}

@JourneyFrameworkComponent
final class InviteJointLandlordStep(
    stepConfig: InviteJointLandlordConfig,
) : RequestableStep<Complete, InviteJointLandlordsFormModel, JointLandlordsState>(stepConfig)
