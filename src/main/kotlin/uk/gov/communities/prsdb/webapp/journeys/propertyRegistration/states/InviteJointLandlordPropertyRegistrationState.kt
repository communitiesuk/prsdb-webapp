package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.shared.states.ConfirmAndInviteJointLandlordState

interface InviteJointLandlordPropertyRegistrationState : ConfirmAndInviteJointLandlordState

enum class AnyLandlordsInvited {
    NO_LANDLORDS,
    SOME_LANDLORDS,
}
