package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.hibernate.exception.ConstraintViolationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.clients.GovUkPayClient
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentFailureType
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperty
import uk.gov.communities.prsdb.webapp.database.entity.Payment
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.exceptions.PaymentQuoteExpiredException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.RenewalDateHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentStatusCheckDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationPaymentCreationResult
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationPaymentQuote
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
    private val savedJourneyStateRepository: SavedJourneyStateRepository,
    private val userToLandlordService: UserToLandlordService,
    private val propertyRegistrationService: PropertyRegistrationService,
    private val paymentReconciliationService: PaymentReconciliationService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val messageSource: MessageSource,
    private val transactionTemplate: TransactionTemplate,
    private val entityManager: EntityManager,
) {
    private val gratisPeriodEndDate: LocalDate = LocalDate.parse(gratisPeriodEndDate)

    fun getPropertyRegistrationPaymentQuote(
        quoteDate: LocalDate = LocalDate.now(DateTimeHelper.UK_ZONE),
    ): PropertyRegistrationPaymentQuote {
        val anniversary = userToLandlordService.getCurrentLandlordForUser().anniversary ?: MonthDay.from(quoteDate)
        val renewalDate = RenewalDateHelper.getRenewalDate(anniversary, quoteDate)

        return PropertyRegistrationPaymentQuote(
            amountInPence = calculateProRatedFeeInPence(renewalDate, quoteDate),
            quoteDate = quoteDate,
            renewalDate = renewalDate,
        )
    }

    fun getAnnualFeeInPence(): Int = annualFeeInPence

    fun getFirstChargeableDate(quoteDate: LocalDate): LocalDate = maxOf(quoteDate, gratisPeriodEndDate.plusDays(1))

    fun createPropertyRegistrationPayment(
        journeyId: String,
        email: String,
        quote: PropertyRegistrationPaymentQuote,
    ): PropertyRegistrationPaymentCreationResult {
        val incompleteProperty = getIncompletePropertyForCurrentUser(journeyId)

        val renewalDate = quote.renewalDate
        val amountInPence = quote.amountInPence
        check(amountInPence > 0) {
            "Cannot create a GOV.UK Pay payment for journey $journeyId: the fee for the period ending $renewalDate is " +
                "${amountInPence}p but GOV.UK Pay only accepts amounts greater than zero"
        }

        val storedStatuses = markCreatedPaymentsForCancellation(incompleteProperty)

        storedStatuses.filterValues { it == PaymentStatus.SUCCEEDED }.keys.firstOrNull()?.let { succeededPaymentId ->
            throw IllegalStateException(
                "Cannot create a GOV.UK Pay payment for journey $journeyId: payment $succeededPaymentId for this " +
                    "property has already succeeded",
            )
        }

        val reference = UUID.randomUUID().toString()
        val returnUrl = absoluteUrlProvider.buildPropertyRegistrationPaymentReturnUri(journeyId, reference).toString()
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

        return try {
            paymentRepository.saveAndFlush(Payment.fromGovUkPay(createdGovUkPayPayment.payment, quote.quoteDate, renewalDate, incompleteProperty))
            PropertyRegistrationPaymentCreationResult.NewPayment(createdGovUkPayPayment.nextUrl)
        } catch (exception: DataIntegrityViolationException) {
            if (!exception.isViolationOf(ONE_CREATED_PAYMENT_PER_DRAFT_CONSTRAINT)) throw exception
            cancelUnsavedPayment(createdGovUkPayPayment.payment.paymentId, journeyId)
            PropertyRegistrationPaymentCreationResult.ConcurrentPaymentStarted
        }
    }

    private fun markCreatedPaymentsForCancellation(incompleteProperty: LandlordIncompleteProperty): Map<String, PaymentStatus> =
        paymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty).associate { storedPayment ->
            storedPayment.paymentId to
                if (storedPayment.status == PaymentStatus.CREATED) {
                    paymentReconciliationService.markForCancellation(storedPayment.paymentId)
                } else {
                    storedPayment.status
                }
        }

    private fun DataIntegrityViolationException.isViolationOf(constraintName: String): Boolean =
        generateSequence<Throwable>(this) { it.cause }
            .filterIsInstance<ConstraintViolationException>()
            .any { it.constraintName.equals(constraintName, ignoreCase = true) }

    private fun cancelUnsavedPayment(
        paymentId: String,
        journeyId: String,
    ) {
        try {
            govUkPayClient.cancelPayment(paymentId)
        } catch (exception: GovUkPayException) {
            println(
                "Could not cancel GOV.UK Pay payment $paymentId, which was not saved because another payment was started for " +
                    "journey $journeyId at the same time: ${exception.stackTraceToString()}",
            )
        }
    }

    fun getPropertyRegistrationPaymentStatus(
        journeyId: String,
        paymentReference: String,
    ): PaymentStatusCheckDataModel {
        val incompleteProperty = getIncompletePropertyForCurrentUser(journeyId)
        val payment =
            checkNotNull(paymentRepository.findByReferenceAndAssociatedIncompleteProperty(paymentReference, incompleteProperty)) {
                "No payment with reference $paymentReference found for journey $journeyId"
            }

        val storedStatus = getStoredPaymentStatus(payment.paymentId)
        if (storedStatus != PaymentStatus.CREATED) {
            return PaymentStatusCheckDataModel(payment.paymentId, storedStatus)
        }
        return getGovUkPayPaymentStatus(payment.paymentId)
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
            failureType = failureType,
        )
    }

    fun getLatestSucceededPaymentForProperty(propertyOwnership: PropertyOwnership): Payment? =
        paymentRepository.findFirstByAssociatedPropertyAndStatusOrderByPaymentCreatedAtDesc(
            propertyOwnership,
            PaymentStatus.SUCCEEDED,
        )

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    fun finalisePayment(
        paymentId: String,
        govUkPayStatus: PaymentStatus,
        registrationData: PropertyRegistrationDataModel,
    ): PaymentStatus =
        when (govUkPayStatus) {
            PaymentStatus.CREATED -> {
                throw IllegalArgumentException("Payment $paymentId cannot be finalised while in progress")
            }

            PaymentStatus.CAPTURABLE -> {
                finaliseCapturablePayment(paymentId, registrationData)
            }

            PaymentStatus.FAILED, PaymentStatus.CANCELLED -> {
                paymentRepository.updateStatusIfCurrentStatusIn(
                    paymentId,
                    PaymentStatus.IN_PROGRESS_STATUSES,
                    govUkPayStatus,
                    Instant.now(),
                )
                getStoredPaymentStatus(paymentId)
            }

            PaymentStatus.SUCCEEDED, PaymentStatus.CANCELLING, PaymentStatus.MANUAL_INTERVENTION -> {
                getStoredPaymentStatus(paymentId)
            }
        }

    private fun finaliseCapturablePayment(
        paymentId: String,
        registrationData: PropertyRegistrationDataModel,
    ): PaymentStatus =
        try {
            registerPropertyAndCapturePayment(paymentId, registrationData)
        } catch (exception: PaymentQuoteExpiredException) {
            println("Marking payment $paymentId for cancellation: ${exception.message}")
            val status = paymentReconciliationService.markForCancellation(paymentId)
            if (status == PaymentStatus.SUCCEEDED) status else throw exception
        } catch (exception: Exception) {
            println("Failed to finalise payment $paymentId, so marking it for cancellation: ${exception.stackTraceToString()}")
            paymentReconciliationService.markForCancellation(paymentId)
        }

    private fun registerPropertyAndCapturePayment(
        paymentId: String,
        registrationData: PropertyRegistrationDataModel,
    ): PaymentStatus =
        checkNotNull(
            transactionTemplate.execute { transactionStatus ->
                // The claim locks the payment's row until this transaction ends, so any other finalisation or
                // cancellation of this payment waits for it, then sees the result.
                val isClaimed =
                    paymentRepository.updateStatusIfCurrentStatusIn(
                        paymentId,
                        listOf(PaymentStatus.CREATED),
                        PaymentStatus.SUCCEEDED,
                        Instant.now(),
                    ) == 1
                if (!isClaimed) return@execute getStoredPaymentStatus(paymentId)

                val storedPayment = checkNotNull(paymentRepository.findByIdOrNull(paymentId)) { "Payment $paymentId not found" }
                entityManager.refresh(storedPayment)
                val incompleteProperty =
                    checkNotNull(storedPayment.associatedIncompleteProperty) { "Payment $paymentId has no incomplete property to register" }

                val property =
                    propertyRegistrationService.registerProperty(registrationData.copy(renewalDate = storedPayment.forPeriodEnding))
                storedPayment.associateWithProperty(property)
                storedPayment.status = PaymentStatus.SUCCEEDED
                paymentRepository.saveAndFlush(storedPayment)

                // Detached here because the @MapsId on its savedJourneyState cascades a persist on flush, undoing the journey's deletion
                entityManager.detach(incompleteProperty)
                savedJourneyStateRepository.delete(incompleteProperty.savedJourneyState)
                paymentRepository.flush()

                checkQuoteDateMatchesRegistrationDate(storedPayment, property)
                check(!transactionStatus.isRollbackOnly) { "Transaction finalising payment $paymentId is marked for rollback" }

                // Capturing the payment can't be rolled back, so nothing else should run in this transaction after it.
                // TODO PDJB-1853: Reconcile the payment if the transaction fails to commit after capture.
                govUkPayClient.capturePayment(paymentId)
                PaymentStatus.SUCCEEDED
            },
        )

    private fun checkQuoteDateMatchesRegistrationDate(
        payment: Payment,
        property: PropertyOwnership,
    ) {
        if (payment.quoteDate != property.registrationDate) {
            throw PaymentQuoteExpiredException(
                "Payment ${payment.paymentId} was quoted on ${payment.quoteDate} so cannot be taken for a property registered on " +
                    "${property.registrationDate}",
            )
        }
    }

    private fun getStoredPaymentStatus(paymentId: String): PaymentStatus =
        checkNotNull(paymentRepository.findStatusByPaymentId(paymentId)) { "Payment $paymentId not found" }

    private fun getIncompletePropertyForCurrentUser(journeyId: String): LandlordIncompleteProperty {
        val baseUserId = SecurityContextHolder.getContext().authentication!!.name
        return checkNotNull(landlordIncompletePropertiesRepository.findBySavedJourneyState_JourneyIdAndUser_Id(journeyId, baseUserId)) {
            "No incomplete property found for journey $journeyId and user $baseUserId"
        }
    }

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

    companion object {
        private const val ONE_CREATED_PAYMENT_PER_DRAFT_CONSTRAINT = "uc_payment_one_created_per_incomplete_property"
    }
}
