package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.clients.GovUkPayClient
import uk.gov.communities.prsdb.webapp.constants.GRATIS_PERIOD_END_DATE
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.temporal.ChronoUnit

@PrsdbWebService
class PaymentService(
    @Value("\${gov-uk-pay.annual-payment-amount-in-pence}") private val annualFeeInPence: Int,
    private val govUkPayClient: GovUkPayClient,
    private val paymentRepository: PaymentRepository,
    private val transactionTemplate: TransactionTemplate,
) {
    fun calculateProRatedFeeInPence(
        renewalDate: LocalDate,
        today: LocalDate = LocalDate.now(DateTimeHelper.UK_ZONE),
    ): Int {
        require(renewalDate.isAfter(today)) { "Renewal date $renewalDate must be after today ($today)" }

        val chargeableDays = ChronoUnit.DAYS.between(today, renewalDate)
        val gratisDays = calculateGratisDays(today)
        val daysInYear = if (chargeablePeriodIncludesLeapDay(today, renewalDate)) 366L else 365L

        val proRatedFeeInPence = annualFeeInPence * (chargeableDays - gratisDays) / daysInYear

        return proRatedFeeInPence.coerceAtLeast(0L).toInt()
    }

    private fun calculateGratisDays(today: LocalDate): Long =
        if (today.isAfter(GRATIS_PERIOD_END_DATE)) 0L else ChronoUnit.DAYS.between(today, GRATIS_PERIOD_END_DATE.plusDays(1))

    private fun chargeablePeriodIncludesLeapDay(
        today: LocalDate,
        renewalDate: LocalDate,
    ): Boolean =
        (today.year..renewalDate.year)
            .filter { Year.isLeap(it.toLong()) }
            .map { LocalDate.of(it, Month.FEBRUARY, 29) }
            .any { !it.isBefore(today) && it.isBefore(renewalDate) }

    // Not transactional so that each stage commits independently, making a successful claim visible to concurrent
    // callers before any side effects happen
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    fun finalisePayment(
        paymentId: String,
        govUkPayStatus: PaymentStatus,
        registerProperty: () -> PropertyOwnership,
    ): PaymentStatus =
        when (govUkPayStatus) {
            PaymentStatus.CREATED -> throw IllegalArgumentException("Payment $paymentId cannot be finalised while in progress")
            PaymentStatus.SUCCEEDED -> getCurrentStatus(paymentId)
            PaymentStatus.FAILED, PaymentStatus.CANCELLED -> {
                paymentRepository.updateStatusIfCurrentStatusIn(
                    paymentId,
                    PaymentStatus.IN_PROGRESS_STATUSES,
                    govUkPayStatus,
                    Instant.now(),
                )
                getCurrentStatus(paymentId)
            }

            PaymentStatus.CAPTURABLE -> claimAndFinalise(paymentId, registerProperty)
        }

    private fun claimAndFinalise(
        paymentId: String,
        registerProperty: () -> PropertyOwnership,
    ): PaymentStatus {
        val isClaimed =
            paymentRepository.updateStatusIfCurrentStatusIn(
                paymentId,
                listOf(PaymentStatus.CREATED),
                PaymentStatus.CAPTURABLE,
                Instant.now(),
            ) == 1
        if (!isClaimed) return getCurrentStatus(paymentId)

        return try {
            registerPropertyAndCapturePayment(paymentId, registerProperty)
        } catch (exception: Exception) {
            cancelUnfinalisedPayment(paymentId, exception)
        }
    }

    private fun registerPropertyAndCapturePayment(
        paymentId: String,
        registerProperty: () -> PropertyOwnership,
    ): PaymentStatus =
        checkNotNull(
            transactionTemplate.execute { transactionStatus ->
                // Also locks the payment row until the transaction ends, so the outcome can't be changed concurrently
                val isStillCapturable =
                    paymentRepository.updateStatusIfCurrentStatusIn(
                        paymentId,
                        listOf(PaymentStatus.CAPTURABLE),
                        PaymentStatus.SUCCEEDED,
                        Instant.now(),
                    ) == 1
                if (!isStillCapturable) return@execute getCurrentStatus(paymentId)

                val property = registerProperty()

                val payment = checkNotNull(paymentRepository.findByIdOrNull(paymentId)) { "Payment $paymentId not found" }
                payment.associateWithProperty(property)
                payment.forPeriodEnding = property.renewalDate
                // Set again as the entity may have been loaded into the persistence context before the update above
                payment.status = PaymentStatus.SUCCEEDED
                paymentRepository.saveAndFlush(payment)

                // A failure caught during registration can leave the transaction unable to commit once the payment is taken
                check(!transactionStatus.isRollbackOnly) { "Transaction finalising payment $paymentId is marked for rollback" }

                // Captured last so that any earlier failure rolls back the registration before money is taken
                govUkPayClient.capturePayment(paymentId)
                PaymentStatus.SUCCEEDED
            },
        )

    private fun cancelUnfinalisedPayment(
        paymentId: String,
        cause: Exception,
    ): PaymentStatus {
        try {
            govUkPayClient.cancelPayment(paymentId)
        } catch (cancelException: Exception) {
            // Leaves the payment capturable, as it must not be marked cancelled unless GOV.UK Pay has cancelled it
            cancelException.addSuppressed(cause)
            throw cancelException
        }
        paymentRepository.updateStatusIfCurrentStatusIn(
            paymentId,
            listOf(PaymentStatus.CAPTURABLE),
            PaymentStatus.CANCELLED,
            Instant.now(),
        )
        return getCurrentStatus(paymentId)
    }

    private fun getCurrentStatus(paymentId: String): PaymentStatus =
        checkNotNull(paymentRepository.findStatusByPaymentId(paymentId)) { "Payment $paymentId not found" }
}
