package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.exceptions.PaymentQuoteExpiredException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationDataModelFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.services.PaymentService

enum class PaymentOutcome {
    SUCCESS,
    FAILURE,
    IN_PROGRESS,
    QUOTE_EXPIRED,
}

@JourneyFrameworkComponent
class PaymentStatusCheckStepConfig(
    private val paymentService: PaymentService,
    private val propertyRegistrationDataModelFactory: PropertyRegistrationDataModelFactory,
    private val addressAvailabilityService: AddressAvailabilityService,
) : AbstractInternalStepConfig<PaymentOutcome, PropertyRegistrationJourneyState>() {
    override fun afterStepIsReached(state: PropertyRegistrationJourneyState) {
        val paymentReference = checkNotNull(state.paymentReference) { "No payment reference for journey ${state.journeyId}" }
        val govUkPayStatus = paymentService.getPropertyRegistrationPaymentStatus(state.journeyId, paymentReference)
        if (govUkPayStatus.isCreated()) {
            state.paymentOutcome = PaymentOutcome.IN_PROGRESS
            return
        }

        val finalisedStatus =
            try {
                paymentService.finalisePayment(
                    govUkPayStatus.paymentId,
                    govUkPayStatus.status,
                    propertyRegistrationDataModelFactory.fromJourneyState(state),
                )
            } catch (exception: PaymentQuoteExpiredException) {
                state.paymentOutcome = PaymentOutcome.QUOTE_EXPIRED
                state.paymentReference = null
                return
            }

        val paymentOutcome =
            when (finalisedStatus) {
                PaymentStatus.SUCCEEDED -> PaymentOutcome.SUCCESS
                PaymentStatus.FAILED, PaymentStatus.CANCELLED -> PaymentOutcome.FAILURE
                PaymentStatus.CREATED, PaymentStatus.CAPTURABLE -> PaymentOutcome.IN_PROGRESS
            }
        state.paymentOutcome = paymentOutcome

        if (paymentOutcome == PaymentOutcome.FAILURE) {
            val addressTask = state.propertyDetailsTask.addressTask
            addressTask.isAddressAlreadyRegistered = addressTask.getAddress().uprn?.let { addressAvailabilityService.isAddressOwned(it) }
        }
    }

    override fun mode(state: PropertyRegistrationJourneyState) = state.paymentOutcome

    override fun resolveNextDestination(
        state: PropertyRegistrationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        if (state.paymentOutcome == PaymentOutcome.SUCCESS) {
            state.deleteJourney()
        }
        if (state.paymentOutcome == PaymentOutcome.FAILURE) {
            val addressTask = state.propertyDetailsTask.addressTask
            if (addressTask.isAddressAlreadyRegistered == true) {
                return Destination(addressTask.alreadyRegisteredStep)
            }
        }
        return defaultDestination
    }
}

@JourneyFrameworkComponent
final class PaymentStatusCheckStep(
    stepConfig: PaymentStatusCheckStepConfig,
) : JourneyStep.InternalStep<PaymentOutcome, PropertyRegistrationJourneyState>(stepConfig)
