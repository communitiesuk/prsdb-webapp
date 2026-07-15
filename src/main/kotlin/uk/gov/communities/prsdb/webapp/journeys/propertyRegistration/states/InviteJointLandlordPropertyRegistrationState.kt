package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyRegistrationInviteJointLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.LandlordInvitingState

interface InviteJointLandlordPropertyRegistrationState : LandlordInvitingState {
    val hasJointLandlordsStep: HasJointLandlordsStep
    val inviteJointLandlordsTask: PropertyRegistrationInviteJointLandlordsTask
}

enum class AnyLandlordsInvited {
    NO_LANDLORDS,
    SOME_LANDLORDS,
}
