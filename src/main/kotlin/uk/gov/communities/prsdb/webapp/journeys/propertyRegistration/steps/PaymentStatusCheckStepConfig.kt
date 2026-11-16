package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.exceptions.CyaDataHasChangedException
import uk.gov.communities.prsdb.webapp.exceptions.PaymentQuoteExpiredException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationDataModelFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.services.PaymentReconciliationService
import uk.gov.communities.prsdb.webapp.services.PaymentService

enum class PaymentOutcome {
    SUCCESS,
    FAILURE,
    NOT_COMPLETED,
    IN_PROGRESS,
    QUOTE_EXPIRED,
}

@JourneyFrameworkComponent
class PaymentStatusCheckStepConfig(
    private val paymentService: PaymentService,
    private val paymentReconciliationService: PaymentReconciliationService,
    private val propertyRegistrationDataModelFactory: PropertyRegistrationDataModelFactory,
    private val addressAvailabilityService: AddressAvailabilityService,
) : AbstractInternalStepConfig<PaymentOutcome, PropertyRegistrationJourneyState>() {
    override fun afterStepIsReached(state: PropertyRegistrationJourneyState) {
        val paymentReference = checkNotNull(state.paymentReference) { "No payment reference for journey ${state.journeyId}" }
        val storedPaymentStatus = paymentService.getStoredPropertyRegistrationPaymentStatus(state.journeyId, paymentReference)
        val paymentStatus =
            if (storedPaymentStatus.isCreated()) {
                try {
                    checkWithGovUkPay(storedPaymentStatus.paymentId, state)
                } catch (exception: PaymentQuoteExpiredException) {
                    state.paymentOutcome = PaymentOutcome.QUOTE_EXPIRED
                    state.paymentReference = null
                    return
                }
            } else {
                storedPaymentStatus.status
            }

        val paymentOutcome =
            when (paymentStatus) {
                PaymentStatus.SUCCEEDED -> PaymentOutcome.SUCCESS
                PaymentStatus.FAILED -> PaymentOutcome.FAILURE
                PaymentStatus.CANCELLING, PaymentStatus.CANCELLED, PaymentStatus.MANUAL_INTERVENTION -> PaymentOutcome.NOT_COMPLETED
                PaymentStatus.CREATED -> PaymentOutcome.IN_PROGRESS
                PaymentStatus.CAPTURABLE -> throw IllegalStateException(
                    "Payment ${storedPaymentStatus.paymentId} for journey ${state.journeyId} is stored as capturable",
                )
            }
        state.paymentOutcome = paymentOutcome

        if (paymentOutcome.isUnsuccessful()) {
            val addressTask = state.propertyDetailsTask.addressTask
            addressTask.isAddressAlreadyRegistered = addressTask.getAddress().uprn?.let { addressAvailabilityService.isAddressOwned(it) }
        }
    }

    private fun checkWithGovUkPay(
        paymentId: String,
        state: PropertyRegistrationJourneyState,
    ): PaymentStatus {
        val govUkPayStatus = paymentService.getGovUkPayPaymentStatus(paymentId).status
        if (govUkPayStatus == PaymentStatus.CREATED) {
            return PaymentStatus.CREATED
        }

        if (govUkPayStatus == PaymentStatus.CAPTURABLE && state.answersChangedSinceConfirmation) {
            val storedStatus = paymentReconciliationService.markForCancellation(paymentId)
            if (storedStatus == PaymentStatus.SUCCEEDED) {
                return storedStatus
            }
            throw CyaDataHasChangedException(
                "Answers changed after confirmation for journey ${state.journeyId}; payment marked for cancellation",
            )
        }

        return paymentService.finalisePayment(paymentId, govUkPayStatus, propertyRegistrationDataModelFactory.fromJourneyState(state))
    }

    override fun mode(state: PropertyRegistrationJourneyState) = state.paymentOutcome

    override fun resolveNextDestination(
        state: PropertyRegistrationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        if (state.paymentOutcome == PaymentOutcome.SUCCESS) {
            state.deleteJourney()
        }
        if (state.paymentOutcome?.isUnsuccessful() == true) {
            val addressTask = state.propertyDetailsTask.addressTask
            if (addressTask.isAddressAlreadyRegistered == true) {
                return Destination(addressTask.alreadyRegisteredStep)
            }
        }
        return defaultDestination
    }

    private fun PaymentOutcome.isUnsuccessful() = this == PaymentOutcome.FAILURE || this == PaymentOutcome.NOT_COMPLETED
}

@JourneyFrameworkComponent
final class PaymentStatusCheckStep(
    stepConfig: PaymentStatusCheckStepConfig,
) : JourneyStep.InternalStep<PaymentOutcome, PropertyRegistrationJourneyState>(stepConfig)
