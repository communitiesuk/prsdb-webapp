package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordsTaskDependencies
import uk.gov.communities.prsdb.webapp.journeys.shared.states.LandlordInvitingState

interface InviteJointLandlordPropertyRegistrationState :
    LandlordInvitingState,
    InviteJointLandlordsTaskDependencies {
    val hasJointLandlordsStep: HasJointLandlordsStep
    val inviteJointLandlordsTask: InviteJointLandlordsTask
}

enum class AnyLandlordsInvited {
    NO_LANDLORDS,
    SOME_LANDLORDS,
}
