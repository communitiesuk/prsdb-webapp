package uk.gov.communities.prsdb.webapp.journeys.shared.states

import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.ConfirmAndInviteJointLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.HasJointLandlordsStep

interface ConfirmAndInviteJointLandlordState : InviteJointLandlordState {
    val hasJointLandlordsStep: HasJointLandlordsStep
    val confirmAndInviteJointLandlordsTask: ConfirmAndInviteJointLandlordsTask
}
