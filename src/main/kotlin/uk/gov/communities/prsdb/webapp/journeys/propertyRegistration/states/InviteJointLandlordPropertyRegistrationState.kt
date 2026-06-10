package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyJointLandlordsInvitedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.InviteJointLandlordState

interface InviteJointLandlordPropertyRegistrationState : InviteJointLandlordState {
    val hasAnyJointLandlordsInvitedStep: HasAnyJointLandlordsInvitedStep
    val hasJointLandlordsStep: HasJointLandlordsStep

    override val checkJointLandlordsBackUrl: String?
        get() = hasAnyJointLandlordsInvitedStep.backUrl
}

enum class AnyLandlordsInvited {
    NO_LANDLORDS,
    SOME_LANDLORDS,
}
