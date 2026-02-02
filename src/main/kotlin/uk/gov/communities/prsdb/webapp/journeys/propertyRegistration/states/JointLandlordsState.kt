package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveJointLandlordStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.InviteJointLandlordsFormModel

interface JointLandlordsState : JourneyState {
    val hasJointLandlordsStep: HasJointLandlordsStep
    val inviteJointLandlordStep: InviteJointLandlordStep
    val checkJointLandlordsStep: CheckJointLandlordsStep
    val removeJointLandlordStep: RemoveJointLandlordStep

    var invitedJointLandlordEmails: List<String>?

    fun getAllInvitedJointLandlordEmails(): List<String> {
        val invitedLandlords = inviteJointLandlordStep.formModel.notNullValue(InviteJointLandlordsFormModel::invitedEmailAddresses)
        val lastInvitedLandlord = inviteJointLandlordStep.formModel.notNullValue(InviteJointLandlordsFormModel::emailAddress)
        return invitedLandlords + lastInvitedLandlord
    }
}
