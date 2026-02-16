package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyJointLandlordsInvitedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveJointLandlordStep

interface JointLandlordsState : JourneyState {
    val hasAnyJointLandlordsInvitedStep: HasAnyJointLandlordsInvitedStep
    val hasJointLandlordsStep: HasJointLandlordsStep
    val inviteJointLandlordStep: InviteJointLandlordStep
    val inviteAnotherJointLandlordStep: InviteJointLandlordStep
    val checkJointLandlordsStep: CheckJointLandlordsStep
    val removeJointLandlordStep: RemoveJointLandlordStep

    var invitedJointLandlordEmails: List<String>?

    val invitedJointLandlords: List<String>
        get() = invitedJointLandlordEmails ?: emptyList()
}

enum class AnyLandlordsInvited {
    NO_LANDLORDS,
    SOME_LANDLORDS,
}
