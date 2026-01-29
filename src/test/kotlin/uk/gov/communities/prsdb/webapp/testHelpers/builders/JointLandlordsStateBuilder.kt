package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasJointLandlordsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.InviteJointLandlordsFormModel

interface JointLandlordsStateBuilder<SelfType : JointLandlordsStateBuilder<SelfType>> {
    val submittedValueMap: MutableMap<String, FormModel>

    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    fun withHasJointLandlordsSetToFalse(): SelfType {
        val hasJointLandlordsFormModel =
            HasJointLandlordsFormModel().apply {
                hasJointLandlords = false
            }
        withSubmittedValue(RegisterPropertyStepId.HasJointLandlords.urlPathSegment, hasJointLandlordsFormModel)
        return self()
    }

    fun withHasNoJointLandlords(): SelfType {
        submittedValueMap.remove(RegisterPropertyStepId.InviteJointLandlord.urlPathSegment)
        return withHasJointLandlordsSetToFalse()
    }

    fun withHasJointLandlords(hasJointLandlords: Boolean): SelfType {
        val hasJointLandlordsFormModel =
            HasJointLandlordsFormModel().apply {
                this.hasJointLandlords = hasJointLandlords
            }
        withSubmittedValue(RegisterPropertyStepId.HasJointLandlords.urlPathSegment, hasJointLandlordsFormModel)
        return self()
    }

    fun withInvitedJointLandlords(emailAddresses: MutableList<String> = mutableListOf<String>("email@address.com")): SelfType {
        val inviteJointLandlordsFormModel =
            InviteJointLandlordsFormModel().apply {
                this.emailAddresses = emailAddresses
            }
        withSubmittedValue(RegisterPropertyStepId.InviteJointLandlord.urlPathSegment, inviteJointLandlordsFormModel)
        return self()
    }

    fun withJointLandlords(emailAddresses: MutableList<String> = mutableListOf<String>("email@address.com")): SelfType {
        withHasJointLandlords(true)
        withInvitedJointLandlords(emailAddresses)
        @Suppress("UNCHECKED_CAST")
        return self()
    }
}
