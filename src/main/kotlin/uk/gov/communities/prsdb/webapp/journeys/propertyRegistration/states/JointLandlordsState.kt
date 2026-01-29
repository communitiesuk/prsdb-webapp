package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveJointLandlordStep

interface JointLandlordsState : JourneyState {
    val hasJointLandlordsStep: HasJointLandlordsStep
    val inviteJointLandlordStep: InviteJointLandlordStep
    val checkJointLandlordsStep: CheckJointLandlordsStep
    val removeJointLandlordStep: RemoveJointLandlordStep

    var invitedJointLandlordEmails: List<String>?
}
