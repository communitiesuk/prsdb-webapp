package uk.gov.communities.prsdb.webapp.journeys.shared.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordsTaskDependencies
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.RemoveJointLandlordAreYouSureStep

interface InviteJointLandlordState : LandlordInvitingState {
    val dependencies: InviteJointLandlordsTaskDependencies

    val inviteJointLandlordStep: InviteJointLandlordStep
    val inviteAnotherJointLandlordStep: InviteJointLandlordStep
    val checkJointLandlordsStep: CheckJointLandlordsStep
    val removeJointLandlordAreYouSureStep: RemoveJointLandlordAreYouSureStep

    var invitedJointLandlordEmailsMap: Map<Int, String>?
    var nextJointLandlordMemberId: Int?

    override val invitedJointLandlords: List<String>
        get() = invitedJointLandlordEmailsMap?.values?.toList() ?: emptyList()
}

interface LandlordInvitingState : JourneyState {
    val invitedJointLandlords: List<String>
}
