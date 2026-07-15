package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyJointLandlordsInvitedStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.RemoveJointLandlordAreYouSureStep
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class PropertyRegistrationInviteJointLandlordsTask(
    journeyStateService: JourneyStateService,
    hasAnyJointLandlordsInvitedStep: HasAnyJointLandlordsInvitedStep,
    inviteJointLandlordStep: InviteJointLandlordStep,
    inviteAnotherJointLandlordStep: InviteJointLandlordStep,
    checkJointLandlordsStep: CheckJointLandlordsStep,
    removeJointLandlordAreYouSureStep: RemoveJointLandlordAreYouSureStep,
    private val landlordService: LandlordService,
) : InviteJointLandlordsTask(
        journeyStateService,
        hasAnyJointLandlordsInvitedStep,
        inviteJointLandlordStep,
        inviteAnotherJointLandlordStep,
        checkJointLandlordsStep,
        removeJointLandlordAreYouSureStep,
    ) {
    override val loggedInLandlordEmail: String?
        // TODO: PDJB-1274: Update emails to account for org landlord
        get() =
            landlordService
                .retrieveLandlordByBaseUserId(SecurityContextHolder.getContext().authentication.name)
                ?.email
}
