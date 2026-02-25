package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.InviteJointLandlordStep
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
        withSubmittedValue(HasJointLandlordsStep.ROUTE_SEGMENT, hasJointLandlordsFormModel)
        return self()
    }

    fun withHasNoJointLandlords(): SelfType {
        submittedValueMap.remove(InviteJointLandlordStep.INVITE_FIRST_ROUTE_SEGMENT)
        return withHasJointLandlordsSetToFalse()
    }

    fun withHasJointLandlords(hasJointLandlords: Boolean): SelfType {
        val hasJointLandlordsFormModel =
            HasJointLandlordsFormModel().apply {
                this.hasJointLandlords = hasJointLandlords
            }
        withSubmittedValue(HasJointLandlordsStep.ROUTE_SEGMENT, hasJointLandlordsFormModel)
        return self()
    }

    fun withInvitedJointLandlords(emailAddresses: MutableList<String> = mutableListOf<String>("email@address.com")): SelfType {
        val inviteJointLandlordsFormModel =
            InviteJointLandlordsFormModel().apply {
                this.invitedEmailAddresses = emailAddresses
            }
        withSubmittedValue(InviteJointLandlordStep.INVITE_FIRST_ROUTE_SEGMENT, inviteJointLandlordsFormModel)
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
