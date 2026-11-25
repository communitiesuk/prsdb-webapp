package uk.gov.communities.prsdb.webapp.journeys.shared.payment

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.exceptions.CyaDataHasChangedException
import uk.gov.communities.prsdb.webapp.exceptions.PaymentQuoteExpiredException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PaymentState
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
) : AbstractInternalStepConfig<PaymentOutcome, PaymentState>() {
    override fun afterStepIsReached(state: PaymentState) {
        val paymentReference = checkNotNull(state.paymentReference) { "No payment reference for journey ${state.journeyId}" }
        val storedPaymentStatus = state.dependencies.paymentHandler.getStoredPaymentStatus(paymentReference)
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
            state.dependencies.isPaymentAddressTaken = state.dependencies.paymentHandler.isAddressTaken()
        }
    }

    private fun checkWithGovUkPay(
        paymentId: String,
        state: PaymentState,
    ): PaymentStatus {
        val govUkPayStatus = paymentService.getGovUkPayPaymentStatus(paymentId).status
        if (govUkPayStatus == PaymentStatus.CREATED) {
            return PaymentStatus.CREATED
        }

        if (govUkPayStatus == PaymentStatus.CAPTURABLE && state.dependencies.answersChangedSinceConfirmation) {
            val storedStatus = paymentReconciliationService.markForCancellation(paymentId)
            if (storedStatus == PaymentStatus.SUCCEEDED) {
                return storedStatus
            }
            throw CyaDataHasChangedException(
                "Answers changed after confirmation for journey ${state.journeyId}; payment marked for cancellation",
            )
        }

        return state.dependencies.paymentHandler.finalisePayment(paymentId, govUkPayStatus)
    }

    override fun mode(state: PaymentState) = state.paymentOutcome

    override fun resolveNextDestination(
        state: PaymentState,
        defaultDestination: Destination,
    ): Destination {
        if (state.paymentOutcome == PaymentOutcome.SUCCESS) {
            // Resolve the exit destination first: it depends on journey state that deleting the journey removes
            val nextUrl =
                checkNotNull(defaultDestination.toUrlStringOrNull()) {
                    "No destination after payment for journey ${state.journeyId}"
                }
            state.deleteJourney()
            return Destination.ExternalUrl(nextUrl)
        }
        if (state.paymentOutcome?.isUnsuccessful() == true && state.dependencies.isPaymentAddressTaken == true) {
            return Destination(state.dependencies.addressTakenStep)
        }
        return defaultDestination
    }

    private fun PaymentOutcome.isUnsuccessful() = this == PaymentOutcome.FAILURE || this == PaymentOutcome.NOT_COMPLETED
}

@JourneyFrameworkComponent
final class PaymentStatusCheckStep(
    stepConfig: PaymentStatusCheckStepConfig,
) : JourneyStep.InternalStep<PaymentOutcome, PaymentState>(stepConfig)
