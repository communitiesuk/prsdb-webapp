package uk.gov.communities.prsdb.webapp.journeys.shared.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyJointLandlordsInvitedStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.IsMarkedAsJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.RemoveJointLandlordAreYouSureStep

interface InviteJointLandlordState : JourneyState {
    val hasAnyJointLandlordsInvitedStep: HasAnyJointLandlordsInvitedStep
    val hasJointLandlordsStep: HasJointLandlordsStep
    val isMarkedAsJointLandlordStep: IsMarkedAsJointLandlordStep
    val inviteJointLandlordStep: InviteJointLandlordStep
    val inviteAnotherJointLandlordStep: InviteJointLandlordStep
    val checkJointLandlordsStep: CheckJointLandlordsStep
    val removeJointLandlordAreYouSureStep: RemoveJointLandlordAreYouSureStep

    var invitedJointLandlordEmailsMap: Map<Int, String>?
    var nextJointLandlordMemberId: Int?
    var propertyMarkedAsJointLandlord: Boolean

    val invitedJointLandlords: List<String>
        get() = invitedJointLandlordEmailsMap?.values?.toList() ?: emptyList()

    val existingInvitedEmails: List<String>
        get() = emptyList()

    val existingLandlordEmails: List<String>
        get() = emptyList()
}
