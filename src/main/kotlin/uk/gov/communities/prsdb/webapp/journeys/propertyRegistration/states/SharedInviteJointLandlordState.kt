package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveJointLandlordAreYouSureStep

interface SharedInviteJointLandlordState : JourneyState {
    val inviteJointLandlordStep: InviteJointLandlordStep
    val inviteAnotherJointLandlordStep: InviteJointLandlordStep
    val checkJointLandlordsStep: CheckJointLandlordsStep
    val removeJointLandlordAreYouSureStep: RemoveJointLandlordAreYouSureStep

    var invitedJointLandlordEmailsMap: Map<Int, String>?
    var nextJointLandlordMemberId: Int?

    val invitedJointLandlords: List<String>
        get() = invitedJointLandlordEmailsMap?.values?.toList() ?: emptyList()
}
