package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyJointLandlordsInvitedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.SharedInviteJointLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.SharedInviteJointLandlordState

interface InviteJointLandlordPropertyRegistrationState : SharedInviteJointLandlordState {
    val hasAnyJointLandlordsInvitedStep: HasAnyJointLandlordsInvitedStep
    val hasJointLandlordsStep: HasJointLandlordsStep
    val inviteJointLandlordsTask: SharedInviteJointLandlordsTask
}

enum class AnyLandlordsInvited {
    NO_LANDLORDS,
    SOME_LANDLORDS,
}
