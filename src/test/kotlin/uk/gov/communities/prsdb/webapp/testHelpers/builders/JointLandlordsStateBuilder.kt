package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasJointLandlordsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.InviteJointLandlordsFormModel

interface JointLandlordsStateBuilder<SelfType : JointLandlordsStateBuilder<SelfType>> {
    val submittedValueMap: MutableMap<String, FormModel>
    val additionalDataMap: MutableMap<String, String>

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
                this.invitedEmailAddresses = emailAddresses
            }
        withSubmittedValue(RegisterPropertyStepId.InviteJointLandlord.urlPathSegment, inviteJointLandlordsFormModel)
        additionalDataMap["invitedJointLandlordEmails"] =
            Json.encodeToString(serializer(), emailAddresses.mapIndexed { index, email -> index to email }.toMap())
        return self()
    }

    fun withJointLandlords(emailAddresses: MutableList<String> = mutableListOf<String>("email@address.com")): SelfType {
        withHasJointLandlords(true)
        withInvitedJointLandlords(emailAddresses)
        @Suppress("UNCHECKED_CAST")
        return self()
    }
}
