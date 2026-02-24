package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.InviteJointLandlordsFormModel

@JourneyFrameworkComponent
class InviteJointLandlordStepConfig : AbstractRequestableStepConfig<Complete, InviteJointLandlordsFormModel, JointLandlordsState>() {
    override val formModelClass = InviteJointLandlordsFormModel::class

    override fun getStepSpecificContent(state: JointLandlordsState) =
        mapOf(
            "fieldSetHeading" to "jointLandlords.inviteJointLandlord.fieldSetHeading",
            "label" to "jointLandlords.inviteJointLandlord.email.label",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: JointLandlordsState): String = "forms/emailForm"

    override fun mode(state: JointLandlordsState) =
        if (state.invitedJointLandlords.isEmpty()) {
            null
        } else {
            Complete.COMPLETE
        }

    override fun enrichSubmittedDataBeforeValidation(
        state: JointLandlordsState,
        formData: PageData,
    ): PageData =
        super.enrichSubmittedDataBeforeValidation(state, formData) +
            (InviteJointLandlordsFormModel::invitedEmailAddresses.name to state.invitedJointLandlords)

    override fun afterStepDataIsAdded(state: JointLandlordsState) {
        val formModel = getFormModelFromState(state)
        val currentMap = state.invitedJointLandlordEmailsMap?.toMutableMap() ?: mutableMapOf()
        val nextKey = (currentMap.keys.maxOrNull() ?: 0) + 1
        formModel.emailAddress?.let { currentMap[nextKey] = it }
        state.invitedJointLandlordEmailsMap = currentMap
        state.inviteJointLandlordStep.clearFormData()
        state.inviteAnotherJointLandlordStep.clearFormData()
    }
}

@JourneyFrameworkComponent
final class InviteJointLandlordStep(
    stepConfig: InviteJointLandlordStepConfig,
) : RequestableStep<Complete, InviteJointLandlordsFormModel, JointLandlordsState>(stepConfig)
