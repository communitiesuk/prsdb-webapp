package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.clients.GovUkPayClient
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentFailureType
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.entity.Payment
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.RenewalDateHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentStatusCheckDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatePaymentRequest
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.MonthDay
import java.time.Year
import java.time.temporal.ChronoUnit
import java.util.UUID

@PrsdbWebService
class PaymentService(
    @Value("\${gov-uk-pay.annual-payment-amount-in-pence}") private val annualFeeInPence: Int,
    @Value("\${gov-uk-pay.gratis-period-end-date}") gratisPeriodEndDate: String,
    private val govUkPayClient: GovUkPayClient,
    private val paymentRepository: PaymentRepository,
    private val landlordIncompletePropertiesRepository: LandlordIncompletePropertiesRepository,
    private val userToLandlordService: UserToLandlordService,
    private val messageSource: MessageSource,
    private val transactionTemplate: TransactionTemplate,
) {
    private val gratisPeriodEndDate: LocalDate = LocalDate.parse(gratisPeriodEndDate)

    fun createPropertyRegistrationPayment(
        journeyId: String,
        returnUrl: String,
        email: String,
    ): String {
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        val incompleteProperty =
            checkNotNull(landlordIncompletePropertiesRepository.findBySavedJourneyState_JourneyIdAndUser_Id(journeyId, baseUserId)) {
                "No incomplete property found for journey $journeyId and user $baseUserId"
            }

        val anniversary = userToLandlordService.getCurrentLandlordForUser().anniversary ?: MonthDay.now(DateTimeHelper.UK_ZONE)
        val renewalDate = RenewalDateHelper.getRenewalDate(anniversary)
        val amountInPence = calculateProRatedFeeInPence(renewalDate)
        check(amountInPence > 0) {
            "Cannot create a GOV.UK Pay payment for journey $journeyId: the fee for the period ending $renewalDate is " +
                "${amountInPence}p but GOV.UK Pay only accepts amounts greater than zero"
        }

        val storedPayments = paymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty)
        val reconciledStoredStatuses =
            storedPayments.associate { storedPayment ->
                storedPayment.paymentId to
                    if (storedPayment.status in PaymentStatus.IN_PROGRESS_STATUSES) {
                        cancelOrReconcilePayment(storedPayment.paymentId, journeyId)
                    } else {
                        storedPayment.status
                    }
            }

        // TODO PDJB-993: Before capturing a payment, check this property has no other SUCCEEDED payment and cancel it if so.
        //  The check below can't catch two registrations of the same property submitted at nearly the same time, because
        //  neither can see the other's payment yet.
        reconciledStoredStatuses.filterValues { it == PaymentStatus.SUCCEEDED }.keys.firstOrNull()?.let { succeededPaymentId ->
            throw IllegalStateException(
                "Cannot create a GOV.UK Pay payment for journey $journeyId: payment $succeededPaymentId for this " +
                    "property has already succeeded",
            )
        }

        val reference = UUID.randomUUID().toString()
        val createdGovUkPayPayment =
            govUkPayClient.createPayment(
                GovUkPayCreatePaymentRequest(
                    amount = amountInPence,
                    reference = reference,
                    description = messageSource.getMessageForKey("registerProperty.paymentDescription"),
                    returnUrl = returnUrl,
                    email = email,
                ),
            )

        paymentRepository.save(Payment.fromGovUkPay(createdGovUkPayPayment.payment, renewalDate, incompleteProperty))

        return createdGovUkPayPayment.nextUrl
    }

    private fun cancelOrReconcilePayment(
        paymentId: String,
        journeyId: String,
    ): PaymentStatus {
        val govUkPayStatusBeforeCancellation = getGovUkPayPaymentStatus(paymentId)
        val govUkPayStatus =
            if (govUkPayStatusBeforeCancellation.isCancellable) {
                cancelOrGetFinishedGovUkPayStatus(paymentId, "before creating a new payment for journey $journeyId")
            } else {
                govUkPayStatusBeforeCancellation.status
            }

        if (govUkPayStatus !in PaymentStatus.IN_PROGRESS_STATUSES) {
            paymentRepository.updateStatusIfCurrentStatusIn(paymentId, PaymentStatus.IN_PROGRESS_STATUSES, govUkPayStatus, Instant.now())
        }
        return getStoredPaymentStatus(paymentId)
    }

    private fun cancelOrGetFinishedGovUkPayStatus(
        paymentId: String,
        errorContext: String,
    ): PaymentStatus =
        try {
            govUkPayClient.cancelPayment(paymentId)
            PaymentStatus.CANCELLED
        } catch (exception: GovUkPayException) {
            val govUkPayStatusAfterFailedCancellation = getGovUkPayPaymentStatus(paymentId)
            if (govUkPayStatusAfterFailedCancellation.isCancellable) {
                throw GovUkPayException(
                    "Could not cancel in-progress GOV.UK Pay payment $paymentId $errorContext: GOV.UK Pay rejected the " +
                        "cancellation but still reports the payment as cancellable",
                    exception,
                )
            }
            govUkPayStatusAfterFailedCancellation.status
        }

    fun getGovUkPayPaymentStatus(paymentId: String): PaymentStatusCheckDataModel {
        val govUkPayPayment = govUkPayClient.getPayment(paymentId)
        val status = PaymentStatus.fromGovUKPayStatus(govUkPayPayment.state.status)
        val failureType =
            if (status.isFailedOrCancelled()) {
                PaymentFailureType.fromGovUkPayCode(govUkPayPayment.state.code)
            } else {
                null
            }

        return PaymentStatusCheckDataModel(
            paymentId = paymentId,
            status = status,
            isCancellable = govUkPayPayment.links.cancelUrl != null,
            failureType = failureType,
        )
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    fun finalisePayment(
        paymentId: String,
        govUkPayStatus: PaymentStatus,
        registerProperty: () -> PropertyOwnership,
    ): PaymentStatus =
        when (govUkPayStatus) {
            PaymentStatus.CREATED -> throw IllegalArgumentException("Payment $paymentId cannot be finalised while in progress")
            PaymentStatus.SUCCEEDED -> getStoredPaymentStatus(paymentId)
            PaymentStatus.FAILED, PaymentStatus.CANCELLED -> {
                paymentRepository.updateStatusIfCurrentStatusIn(
                    paymentId,
                    PaymentStatus.IN_PROGRESS_STATUSES,
                    govUkPayStatus,
                    Instant.now(),
                )
                getStoredPaymentStatus(paymentId)
            }

            PaymentStatus.CAPTURABLE -> finaliseCapturablePayment(paymentId, registerProperty)
        }

    private fun finaliseCapturablePayment(
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
        if (!isClaimed) return getStoredPaymentStatus(paymentId)

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
                val isStillCapturable =
                    paymentRepository.updateStatusIfCurrentStatusIn(
                        paymentId,
                        listOf(PaymentStatus.CAPTURABLE),
                        PaymentStatus.SUCCEEDED,
                        Instant.now(),
                    ) == 1
                if (!isStillCapturable) return@execute getStoredPaymentStatus(paymentId)

                val property = registerProperty()

                val storedPayment = checkNotNull(paymentRepository.findByIdOrNull(paymentId)) { "Payment $paymentId not found" }
                storedPayment.associateWithProperty(property)
                storedPayment.forPeriodEnding = property.renewalDate
                storedPayment.status = PaymentStatus.SUCCEEDED
                paymentRepository.saveAndFlush(storedPayment)

                check(!transactionStatus.isRollbackOnly) { "Transaction finalising payment $paymentId is marked for rollback" }

                govUkPayClient.capturePayment(paymentId)
                PaymentStatus.SUCCEEDED
            },
        )

    private fun cancelUnfinalisedPayment(
        paymentId: String,
        cause: Exception,
    ): PaymentStatus {
        val govUkPayStatus =
            try {
                cancelOrGetFinishedGovUkPayStatus(paymentId, "after failing to finalise it")
            } catch (cancelException: Exception) {
                cancelException.addSuppressed(cause)
                throw cancelException
            }

        if (!govUkPayStatus.isFailedOrCancelled()) {
            throw IllegalStateException("Payment $paymentId could not be finalised, but GOV.UK Pay reports it as $govUkPayStatus", cause)
        }

        paymentRepository.updateStatusIfCurrentStatusIn(
            paymentId,
            listOf(PaymentStatus.CAPTURABLE),
            govUkPayStatus,
            Instant.now(),
        )
        return getStoredPaymentStatus(paymentId)
    }

    private fun getStoredPaymentStatus(paymentId: String): PaymentStatus =
        checkNotNull(paymentRepository.findStatusByPaymentId(paymentId)) { "Payment $paymentId not found" }

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
        if (today.isAfter(gratisPeriodEndDate)) 0L else ChronoUnit.DAYS.between(today, gratisPeriodEndDate.plusDays(1))

    private fun chargeablePeriodIncludesLeapDay(
        today: LocalDate,
        renewalDate: LocalDate,
    ): Boolean =
        (today.year..renewalDate.year)
            .filter { Year.isLeap(it.toLong()) }
            .map { LocalDate.of(it, Month.FEBRUARY, 29) }
            .any { !it.isBefore(today) && it.isBefore(renewalDate) }
}
