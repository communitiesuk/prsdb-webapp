package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.InviteJointLandlordState

interface InviteJointLandlordPropertyRegistrationState : InviteJointLandlordState {
    val inviteJointLandlordsTask: InviteJointLandlordsTask
}

enum class AnyLandlordsInvited {
    NO_LANDLORDS,
    SOME_LANDLORDS,
}
