package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.inviteJointLandlord

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyJointLandlordsInvitedStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.RemoveJointLandlordAreYouSureStep
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

// InviteJointLandlordsTask specialised for the standalone "invite joint landlord" update journey: the property already
// exists, so the pre-existing invited emails and co-landlord emails are looked up by propertyId (used to prevent
// duplicate/self invitations). propertyId is owned here as the task's own route-scoped state; the journey forwards to it.
@JourneyFrameworkComponent
class UpdateInviteJointLandlordsTask(
    journeyStateService: JourneyStateService,
    hasAnyJointLandlordsInvitedStep: HasAnyJointLandlordsInvitedStep,
    inviteJointLandlordStep: InviteJointLandlordStep,
    inviteAnotherJointLandlordStep: InviteJointLandlordStep,
    checkJointLandlordsStep: CheckJointLandlordsStep,
    removeJointLandlordAreYouSureStep: RemoveJointLandlordAreYouSureStep,
    private val jointLandlordInvitationService: JointLandlordInvitationService,
    private val propertyOwnershipService: PropertyOwnershipService,
) : InviteJointLandlordsTask(
        journeyStateService,
        hasAnyJointLandlordsInvitedStep,
        inviteJointLandlordStep,
        inviteAnotherJointLandlordStep,
        checkJointLandlordsStep,
        removeJointLandlordAreYouSureStep,
    ) {
    var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")

    override val existingInvitedEmails: List<String>
        get() = jointLandlordInvitationService.getExistingInvitedEmails(propertyId)

    // TODO: PDJB-1274: Update emails to account for org landlord
    override val existingLandlordEmails: List<String>
        get() = propertyOwnershipService.getPropertyOwnership(propertyId).landlords.map { (it as IndividualLandlord).email }
}
