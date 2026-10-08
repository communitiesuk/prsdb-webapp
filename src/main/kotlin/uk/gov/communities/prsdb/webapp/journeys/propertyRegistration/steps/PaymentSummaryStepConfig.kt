package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.services.PaymentService

@JourneyFrameworkComponent
class PaymentSummaryStepConfig(
    private val addressAvailabilityService: AddressAvailabilityService,
    private val paymentService: PaymentService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, PropertyRegistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    // TODO PDJB-996: Replace this stub with the real payment summary page (shows the amount the user has to pay).
    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState): Map<String, Any?> =
        mapOf("todoComment" to "Payment summary (TODO PDJB-996)")

    override fun chooseTemplate(state: PropertyRegistrationJourneyState) = "forms/todo"

    override fun mode(state: PropertyRegistrationJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepDataIsAdded(state: PropertyRegistrationJourneyState) {
        val addressTask = state.propertyDetailsTask.addressTask
        val uprn = addressTask.getAddress().uprn
        if (uprn != null && addressAvailabilityService.isAddressOwned(uprn)) {
            addressTask.isAddressAlreadyRegistered = true
        }
    }

    override fun resolveNextDestination(
        state: PropertyRegistrationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        val addressTask = state.propertyDetailsTask.addressTask
        if (addressTask.isAddressAlreadyRegistered == true) {
            return Destination(addressTask.alreadyRegisteredStep)
        }

        state.paymentOutcome = null
        val nextUrl = paymentService.createPropertyRegistrationPayment(state.journeyId, state.loggedInLandlordEmailAtStartOfJourney)
        return Destination.ExternalUrl(nextUrl)
    }
}

@JourneyFrameworkComponent
final class PaymentSummaryStep(
    stepConfig: PaymentSummaryStepConfig,
) : RequestableStep<Complete, NoInputFormModel, PropertyRegistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "payment-summary"
    }
}
