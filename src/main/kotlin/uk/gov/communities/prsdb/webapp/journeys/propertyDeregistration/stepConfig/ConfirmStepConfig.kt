package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyDeregistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class ConfirmStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyDeregistrationService: PropertyDeregistrationService,
    private val confirmationEmailSender: EmailNotificationService<PropertyDeregistrationConfirmationEmail>,
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
        val emailDetails = propertyDeregistrationService.deregisterProperty(state.propertyOwnershipId)
        propertyDeregistrationService.addDeregisteredPropertyOwnershipIdToSession(
            state.propertyOwnershipId,
            emailDetails.singleLineAddress,
        )

        // PDJB-318: Use new email here
        for (landlordEmail in emailDetails.landlordEmailAddresses)
            confirmationEmailSender.sendEmail(
                landlordEmail,
                PropertyDeregistrationConfirmationEmail(
                    emailDetails.prn,
                    emailDetails.singleLineAddress,
                ),
            )
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
