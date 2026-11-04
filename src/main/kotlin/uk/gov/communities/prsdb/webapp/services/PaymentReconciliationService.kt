package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.client.ResourceAccessException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.clients.GovUkPayClient
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentManualInterventionReason
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.helpers.EmailHelper.Companion.sendEmailAndLogFailures
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PaymentRequiresManualInterventionEmail
import java.time.Duration
import java.time.Instant

@PrsdbWebService
class PaymentReconciliationService(
    @Value("\${gov-uk-pay.manual-intervention-email-address}") private val manualInterventionEmailAddress: String,
    @Value("\${gov-uk-pay.payment-status-retry-delay}") private val paymentStatusRetryDelay: Duration,
    private val paymentRepository: PaymentRepository,
    private val govUkPayClient: GovUkPayClient,
    private val manualInterventionEmailSender: EmailNotificationService<PaymentRequiresManualInterventionEmail>,
) {
    // Marking the payment waits for any finalisation of it that is in progress, because that holds the row lock from its
    // CREATED -> SUCCEEDED claim. A CANCELLING payment can never be captured.
    // Give a reason if the payment may have been taken, so support is emailed when GOV.UK Pay won't cancel it.
    fun markForCancellation(
        paymentId: String,
        manualInterventionReasonIfNotCancelled: PaymentManualInterventionReason? = null,
    ): PaymentStatus {
        val isMarked =
            paymentRepository.updateStatusIfCurrentStatusIs(
                paymentId,
                PaymentStatus.CREATED,
                PaymentStatus.CANCELLING,
                Instant.now(),
            ) == 1

        if (isMarked) {
            try {
                govUkPayClient.cancelPayment(paymentId)
                paymentRepository.updateStatusIfCurrentStatusIs(
                    paymentId,
                    PaymentStatus.CANCELLING,
                    PaymentStatus.CANCELLED,
                    Instant.now(),
                )
            } catch (exception: GovUkPayException) {
                if (manualInterventionReasonIfNotCancelled != null) {
                    println(
                        "Could not cancel GOV.UK Pay payment $paymentId, which may have been taken: " +
                            exception.stackTraceToString(),
                    )
                    return recordManualIntervention(
                        paymentId,
                        manualInterventionReasonIfNotCancelled,
                        expectedStatus = PaymentStatus.CANCELLING,
                    )
                }
                println(
                    "Could not cancel GOV.UK Pay payment $paymentId, so leaving it CANCELLING: " +
                        exception.stackTraceToString(),
                )
            }
        }

        return getStoredPaymentStatus(paymentId)
    }

    fun recordManualIntervention(
        paymentId: String,
        reason: PaymentManualInterventionReason,
        expectedStatus: PaymentStatus,
    ): PaymentStatus {
        val isRecorded =
            paymentRepository.updateStatusIfCurrentStatusIs(
                paymentId,
                expectedStatus,
                PaymentStatus.MANUAL_INTERVENTION,
                Instant.now(),
            ) == 1

        if (isRecorded) {
            println("Payment $paymentId requires manual intervention: ${reason.description}")
            sendEmailAndLogFailures("the manual intervention email for payment $paymentId") {
                emailSupportAboutManualIntervention(paymentId, reason)
            }
        }

        return getStoredPaymentStatus(paymentId)
    }

    fun getGovUkPayPaymentStatusWithRetry(paymentId: String): PaymentStatus =
        try {
            getGovUkPayPaymentStatus(paymentId)
        } catch (exception: GovUkPayException) {
            if (!exception.isTransient()) throw exception
            waitBeforeRetrying(exception)
            try {
                getGovUkPayPaymentStatus(paymentId)
            } catch (retryException: GovUkPayException) {
                retryException.addSuppressed(exception)
                throw retryException
            }
        }

    private fun getGovUkPayPaymentStatus(paymentId: String): PaymentStatus =
        PaymentStatus.fromGovUKPayStatus(govUkPayClient.getPayment(paymentId).state.status)

    private fun GovUkPayException.isTransient(): Boolean = httpStatus?.is5xxServerError() ?: (cause is ResourceAccessException)

    private fun waitBeforeRetrying(exception: GovUkPayException) {
        try {
            Thread.sleep(paymentStatusRetryDelay)
        } catch (interruption: InterruptedException) {
            Thread.currentThread().interrupt()
            exception.addSuppressed(interruption)
            throw exception
        }
    }

    private fun emailSupportAboutManualIntervention(
        paymentId: String,
        reason: PaymentManualInterventionReason,
    ) {
        val payment = checkNotNull(paymentRepository.findByIdOrNull(paymentId)) { "Payment $paymentId not found" }
        manualInterventionEmailSender.sendEmail(
            manualInterventionEmailAddress,
            PaymentRequiresManualInterventionEmail(
                payment.paymentId,
                payment.reference,
                payment.amountInPence,
                payment.paymentCreatedAt,
                reason.description,
            ),
        )
    }

    private fun getStoredPaymentStatus(paymentId: String): PaymentStatus =
        checkNotNull(paymentRepository.findStatusByPaymentId(paymentId)) { "Payment $paymentId not found" }
}
