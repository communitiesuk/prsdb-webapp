package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.PageData
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.InviteJointLandlordsFormModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

@JourneyFrameworkComponent
class InviteJointLandlordStepConfig(
    private val urlParameterService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<Complete, InviteJointLandlordsFormModel, JointLandlordsState>() {
    override val formModelClass = InviteJointLandlordsFormModel::class

    override fun getStepSpecificContent(state: JointLandlordsState): Map<String, Any?> =
        mutableMapOf(
            "fieldSetHeading" to "jointLandlords.inviteJointLandlord.fieldSetHeading",
            "label" to "jointLandlords.inviteJointLandlord.email.label",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun resolvePageContent(
        state: JointLandlordsState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> {
        val formModel = defaultContent[FORM_MODEL_ATTR_NAME] as? InviteJointLandlordsFormModel
        if (!formModel?.emailAddress.isNullOrBlank()) return defaultContent

        val prepopulatedEmail = getEmailToEditOrNull(state) ?: return defaultContent
        val prepopulatedFormModel = formModel ?: InviteJointLandlordsFormModel()
        prepopulatedFormModel.emailAddress = prepopulatedEmail
        return defaultContent + (FORM_MODEL_ATTR_NAME to prepopulatedFormModel)
    }

    override fun chooseTemplate(state: JointLandlordsState): String = "forms/emailForm"

    override fun mode(state: JointLandlordsState) =
        if (state.invitedJointLandlords.isEmpty()) {
            null
        } else {
            Complete.COMPLETE
        }

    override fun beforeAttemptingToReachStep(state: JointLandlordsState): Boolean {
        val keyToUpdate = urlParameterService.getParameterOrNull() ?: return true

        val currentMap = state.invitedJointLandlordEmailsMap ?: emptyMap()

        return keyToUpdate in currentMap.keys
    }

    override fun enrichSubmittedDataBeforeValidation(
        state: JointLandlordsState,
        formData: PageData,
    ): PageData {
        val emailBeingEdited = getEmailToEditOrNull(state)

        return super.enrichSubmittedDataBeforeValidation(state, formData) +
            (InviteJointLandlordsFormModel::invitedEmailAddresses.name to state.invitedJointLandlords) +
            (InviteJointLandlordsFormModel::emailBeingEdited.name to emailBeingEdited)
    }

    override fun afterStepDataIsAdded(state: JointLandlordsState) {
        val formModel = getFormModelFromState(state)
        val currentMap = state.invitedJointLandlordEmailsMap?.toMutableMap() ?: mutableMapOf()

        val keyToUpdate = urlParameterService.getParameterOrNull()
        if (keyToUpdate != null) {
            formModel.emailAddress?.let { currentMap[keyToUpdate] = it }
        } else {
            // We need entries to have unique indexes as if a user goes back to the delete page of an old landlord, we want to ensure they can't delete a landlord they didn't mean to
            val nextKey = state.nextJointLandlordMemberId ?: ((currentMap.keys.maxOrNull() ?: 0) + 1)
            formModel.emailAddress?.let {
                currentMap[nextKey] = it
                state.nextJointLandlordMemberId = nextKey + 1
            }
        }
        state.invitedJointLandlordEmailsMap = currentMap
        state.inviteJointLandlordStep.clearFormData()
        state.inviteAnotherJointLandlordStep.clearFormData()
    }

    private fun getEmailToEditOrNull(state: JointLandlordsState): String? {
        val keyToUpdate = urlParameterService.getParameterOrNull() ?: return null

        return state.invitedJointLandlordEmailsMap?.get(keyToUpdate)
    }
}

@JourneyFrameworkComponent
final class InviteJointLandlordStep(
    stepConfig: InviteJointLandlordStepConfig,
) : RequestableStep<Complete, InviteJointLandlordsFormModel, JointLandlordsState>(stepConfig) {
    companion object {
        const val INVITE_FIRST_ROUTE_SEGMENT = "invite-joint-landlord"
        const val INVITE_ANOTHER_ROUTE_SEGMENT = "invite-another-joint-landlord"
    }
}
