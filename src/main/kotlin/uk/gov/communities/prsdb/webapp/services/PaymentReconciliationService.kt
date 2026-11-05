package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.clients.GovUkPayClient
import uk.gov.communities.prsdb.webapp.constants.ABANDONED_PAYMENT_AGE_IN_HOURS
import uk.gov.communities.prsdb.webapp.constants.UNRESOLVED_CANCELLING_PAYMENT_AGE_IN_HOURS
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentManualInterventionReason
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.helpers.EmailHelper.Companion.sendEmailAndLogFailures
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PaymentRequiresManualInterventionEmail
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PaymentReconciliationService(
    @Value("\${gov-uk-pay.manual-intervention-email-address}") private val manualInterventionEmailAddress: String,
    private val paymentRepository: PaymentRepository,
    private val govUkPayClient: GovUkPayClient,
    private val manualInterventionEmailSender: EmailNotificationService<PaymentRequiresManualInterventionEmail>,
) {
    // Marking the payment waits for any finalisation of it that is in progress, because that holds the row lock from its
    // CREATED -> SUCCEEDED claim. A CANCELLING payment can never be captured.
    fun markForCancellation(paymentId: String): PaymentStatus {
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
        if (writeManualIntervention(paymentId, reason, expectedStatus)) {
            sendEmailAndLogFailures("the manual intervention email for payment $paymentId") {
                emailSupportAboutManualIntervention(paymentId, reason)
            }
        }

        return getStoredPaymentStatus(paymentId)
    }

    // Returns the failure count. Never registers a property, so a payment taken after cancellation was requested goes to support.
    fun reconcilePayments(now: Instant): Int {
        var failureCount = 0

        val abandonedPaymentCutoff = now.minus(ABANDONED_PAYMENT_AGE_IN_HOURS, ChronoUnit.HOURS)
        paymentRepository.findAllByStatusAndPaymentCreatedAtBefore(PaymentStatus.CREATED, abandonedPaymentCutoff).forEach { payment ->
            try {
                markForCancellation(payment.paymentId)
            } catch (exception: Exception) {
                println("Failed to mark abandoned payment ${payment.paymentId} for cancellation: ${exception.stackTraceToString()}")
                failureCount++
            }
        }

        val unresolvedPaymentCutoff = now.minus(UNRESOLVED_CANCELLING_PAYMENT_AGE_IN_HOURS, ChronoUnit.HOURS)
        paymentRepository.findAllByStatus(PaymentStatus.CANCELLING).forEach { payment ->
            try {
                val isStillCancelling = resolveCancellingPayment(payment.paymentId)
                if (isStillCancelling && payment.paymentCreatedAt.isBefore(unresolvedPaymentCutoff)) {
                    println("Payment ${payment.paymentId} is still CANCELLING, but GOV.UK Pay should have expired it")
                    failureCount++
                }
            } catch (exception: Exception) {
                println("Failed to resolve cancelling payment ${payment.paymentId}: ${exception.stackTraceToString()}")
                failureCount++
            }
        }

        println("Reconciled payments with $failureCount failures")
        return failureCount
    }

    // Returns whether the payment is left CANCELLING for the next run
    private fun resolveCancellingPayment(paymentId: String): Boolean =
        when (val govUkPayStatus = cancelOrReadGovUkPayStatus(paymentId)) {
            PaymentStatus.CANCELLED, PaymentStatus.FAILED -> {
                paymentRepository.updateStatusIfCurrentStatusIs(paymentId, PaymentStatus.CANCELLING, govUkPayStatus, Instant.now())
                false
            }
            PaymentStatus.SUCCEEDED -> {
                // The job won't see this payment again, so an email failure is thrown to be counted rather than only logged
                val reason = PaymentManualInterventionReason.TAKEN_AFTER_CANCELLATION_REQUESTED
                if (writeManualIntervention(paymentId, reason, PaymentStatus.CANCELLING)) {
                    emailSupportAboutManualIntervention(paymentId, reason)
                }
                false
            }
            null -> true
            else -> {
                println("GOV.UK Pay reports payment $paymentId as $govUkPayStatus, so leaving it CANCELLING")
                true
            }
        }

    // Returns null if GOV.UK Pay did not cancel the payment and its status could not be read
    private fun cancelOrReadGovUkPayStatus(paymentId: String): PaymentStatus? =
        try {
            govUkPayClient.cancelPayment(paymentId)
            PaymentStatus.CANCELLED
        } catch (cancelException: GovUkPayException) {
            println("GOV.UK Pay did not cancel payment $paymentId, so reading its status: ${cancelException.stackTraceToString()}")
            try {
                PaymentStatus.fromGovUKPayStatus(govUkPayClient.getPaymentWithRetry(paymentId).state.status)
            } catch (readException: GovUkPayException) {
                println("Failed to read the status of payment $paymentId, so leaving it CANCELLING: ${readException.stackTraceToString()}")
                null
            }
        }

    private fun writeManualIntervention(
        paymentId: String,
        reason: PaymentManualInterventionReason,
        expectedStatus: PaymentStatus,
    ): Boolean {
        val isRecorded =
            paymentRepository.updateStatusIfCurrentStatusIs(
                paymentId,
                expectedStatus,
                PaymentStatus.MANUAL_INTERVENTION,
                Instant.now(),
            ) == 1

        if (isRecorded) {
            println("Payment $paymentId requires manual intervention: ${reason.description}")
        }
        return isRecorded
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
