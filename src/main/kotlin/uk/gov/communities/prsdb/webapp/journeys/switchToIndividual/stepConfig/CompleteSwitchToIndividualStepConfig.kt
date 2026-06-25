package uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.stepConfig

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.SWITCHED_TO_INDIVIDUAL_PROPERTY_ID
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.SwitchToIndividualJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationCancellationInviteeEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.SwitchToIndividualConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class CompleteSwitchToIndividualStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val jointLandlordInvitationService: JointLandlordInvitationService,
    private val inviteCancellationEmailSender: EmailNotificationService<JointLandlordInvitationCancellationInviteeEmail>,
    private val switchToIndividualConfirmationEmailSender: EmailNotificationService<SwitchToIndividualConfirmationEmail>,
    private val session: HttpSession,
) : AbstractInternalStepConfig<Complete, SwitchToIndividualJourneyState>() {
    override fun mode(state: SwitchToIndividualJourneyState): Complete = Complete.COMPLETE

    @Transactional
    override fun afterStepIsReached(state: SwitchToIndividualJourneyState) {
        val propertyOwnershipId = state.propertyOwnershipId
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)

        if (propertyOwnership.landlords.size > 1) {
            throw PrsdbWebException("Cannot switch to individual: property $propertyOwnershipId has more than one landlord")
        }

        val pendingInvitations = jointLandlordInvitationService.getPendingInvitations(propertyOwnership)
        val propertyAddress = propertyOwnership.address.toMultiLineAddress()

        propertyOwnershipService.markAsNotJointLandlord(propertyOwnership)
        jointLandlordInvitationService.removeInvitations(pendingInvitations)

        val landlord = propertyOwnership.landlords.single()
        switchToIndividualConfirmationEmailSender.sendEmail(
            landlord.email,
            SwitchToIndividualConfirmationEmail(landlordName = landlord.name, propertyAddress = propertyAddress),
        )

        for (invitation in pendingInvitations) {
            inviteCancellationEmailSender.sendEmail(
                invitation.invitedEmail,
                JointLandlordInvitationCancellationInviteeEmail(propertyAddress = propertyAddress),
            )
        }

        session.setAttribute(SWITCHED_TO_INDIVIDUAL_PROPERTY_ID, propertyOwnershipId)
    }

    override fun resolveNextDestination(
        state: SwitchToIndividualJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteSwitchToIndividualStep(
    stepConfig: CompleteSwitchToIndividualStepConfig,
) : JourneyStep.InternalStep<Complete, SwitchToIndividualJourneyState>(stepConfig)
