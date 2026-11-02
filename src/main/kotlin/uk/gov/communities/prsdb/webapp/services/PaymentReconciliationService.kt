package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.clients.GovUkPayClient
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import java.time.Instant

@PrsdbWebService
class PaymentReconciliationService(
    private val paymentRepository: PaymentRepository,
    private val govUkPayClient: GovUkPayClient,
) {
    // Marking the payment waits for any finalisation of it that is in progress, because that holds the row lock from its
    // CREATED -> SUCCEEDED claim. A CANCELLING payment has not been captured and never will be.
    fun markForCancellation(paymentId: String): PaymentStatus {
        val isMarked =
            paymentRepository.updateStatusIfCurrentStatusIn(
                paymentId,
                listOf(PaymentStatus.CREATED),
                PaymentStatus.CANCELLING,
                Instant.now(),
            ) == 1

        if (isMarked) {
            try {
                govUkPayClient.cancelPayment(paymentId)
                paymentRepository.updateStatusIfCurrentStatusIn(
                    paymentId,
                    listOf(PaymentStatus.CANCELLING),
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

    private fun getStoredPaymentStatus(paymentId: String): PaymentStatus =
        checkNotNull(paymentRepository.findStatusByPaymentId(paymentId)) { "Payment $paymentId not found" }
}
