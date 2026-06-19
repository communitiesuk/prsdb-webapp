package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDeregistrationConfirmationEmailRedesign
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDeregistrationInviteeCancellationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyDeregistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class ConfirmStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyDeregistrationService: PropertyDeregistrationService,
    private val jointLandlordInvitationService: JointLandlordInvitationService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val confirmationEmailSender: EmailNotificationService<PropertyDeregistrationConfirmationEmailRedesign>,
    private val inviteeCancellationEmailSender: EmailNotificationService<PropertyDeregistrationInviteeCancellationEmail>,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, PropertyDeregistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PropertyDeregistrationJourneyState) =
        mapOf(
            "address" to
                propertyOwnershipService
                    .getPropertyOwnership(state.propertyOwnershipId)
                    .address
                    .toMultiLineAddress()
                    .split("\n"),
            "cancelLinkUrl" to PropertyDetailsController.getPropertyDetailsPath(state.propertyOwnershipId),
        )

    override fun chooseTemplate(state: PropertyDeregistrationJourneyState) = "forms/confirmPropertyDeregistrationForm"

    override fun mode(state: PropertyDeregistrationJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepDataIsAdded(state: PropertyDeregistrationJourneyState) {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyOwnershipId)

        // This journey is only reached when the user is the last landlord on the record, so landlordContacts
        // currently contains a single landlord.
        if (propertyOwnership.landlords.size != 1) {
            throw UnrecoverableJourneyStateException(
                state.journeyId,
                "There should be no joint landlords on the property if this step of deregistration is reached",
            )
        }
        val landlordContacts = propertyOwnership.landlords.map { it.name to it.email }
        val cancelledInvitationEmailAddresses =
            jointLandlordInvitationService.getPendingInvitations(propertyOwnership).map { it.invitedEmail }
        val singleLineAddress = propertyOwnership.address.singleLineAddress
        val multiLineAddress = propertyOwnership.address.toMultiLineAddress()

        propertyDeregistrationService.deregisterProperty(state.propertyOwnershipId)
        propertyDeregistrationService.addDeregisteredPropertyOwnershipIdToSession(state.propertyOwnershipId, singleLineAddress)

        landlordContacts.forEach { (landlordName, landlordEmail) ->
            confirmationEmailSender.sendEmail(
                landlordEmail,
                PropertyDeregistrationConfirmationEmailRedesign(landlordName, multiLineAddress),
            )
        }

        if (cancelledInvitationEmailAddresses.isNotEmpty()) {
            val signInUrl = absoluteUrlProvider.buildLandlordDashboardUri().toString()
            cancelledInvitationEmailAddresses.forEach { inviteeEmail ->
                inviteeCancellationEmailSender.sendEmail(
                    inviteeEmail,
                    PropertyDeregistrationInviteeCancellationEmail(multiLineAddress, signInUrl),
                )
            }
        }
    }

    override fun resolveNextDestination(
        state: PropertyDeregistrationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        // Clean up journey state from session after all business logic completes
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
final class ConfirmStep(
    stepConfig: ConfirmStepConfig,
) : RequestableStep<Complete, NoInputFormModel, PropertyDeregistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "confirm"
    }
}
