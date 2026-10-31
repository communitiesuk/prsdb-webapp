package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.client.ResourceAccessException
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
import uk.gov.communities.prsdb.webapp.exceptions.PaymentCaptureOutcomeUnknownException
import uk.gov.communities.prsdb.webapp.exceptions.PaymentNotCapturedException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.RenewalDateHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentStatusCheckDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationPaymentCreationResult
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationPaymentQuote
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatePaymentRequest
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PaymentRequiresManualInterventionEmail
import java.time.Duration
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
    @Value("\${gov-uk-pay.payment-status-retry-delay}") private val paymentStatusRetryDelay: Duration,
    @Value("\${gov-uk-pay.manual-intervention-email-address}") private val manualInterventionEmailAddress: String,
    private val govUkPayClient: GovUkPayClient,
    private val paymentRepository: PaymentRepository,
    private val landlordIncompletePropertiesRepository: LandlordIncompletePropertiesRepository,
    private val savedJourneyStateRepository: SavedJourneyStateRepository,
    private val userToLandlordService: UserToLandlordService,
    private val propertyRegistrationService: PropertyRegistrationService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val messageSource: MessageSource,
    private val transactionTemplate: TransactionTemplate,
    private val entityManager: EntityManager,
    private val manualInterventionEmailSender: EmailNotificationService<PaymentRequiresManualInterventionEmail>,
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
        val storedPayments = paymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty)
        val inProgressPayments = checkWithGovUkPay(storedPayments.filter { it.status in PaymentStatus.IN_PROGRESS_STATUSES })

        inProgressPayments.firstOrNull { it.isRecoverable }?.let {
            return PropertyRegistrationPaymentCreationResult.ExistingPayment(it.payment.reference)
        }

        val renewalDate = quote.renewalDate
        val amountInPence = quote.amountInPence
        check(amountInPence > 0) {
            "Cannot create a GOV.UK Pay payment for journey $journeyId: the fee for the period ending $renewalDate is " +
                "${amountInPence}p but GOV.UK Pay only accepts amounts greater than zero"
        }

        val reconciledStoredStatuses = storedPayments.associate { it.paymentId to it.status }.toMutableMap()
        for ((payment, govUkPayStatusBeforeCancellation) in inProgressPayments) {
            val govUkPayStatus =
                if (govUkPayStatusBeforeCancellation.isCancellable) {
                    cancelOrGetFinishedGovUkPayStatus(payment.paymentId, "before creating a new payment for journey $journeyId")
                } else {
                    govUkPayStatusBeforeCancellation.status
                }

            if (payment.status == PaymentStatus.INDETERMINATE && govUkPayStatus == PaymentStatus.SUCCEEDED) {
                return PropertyRegistrationPaymentCreationResult.ExistingPayment(payment.reference)
            }

            reconciledStoredStatuses[payment.paymentId] = recordGovUkPayStatus(payment.paymentId, govUkPayStatus, journeyId)
        }

        reconciledStoredStatuses.filterValues { it == PaymentStatus.SUCCEEDED }.keys.firstOrNull()?.let { succeededPaymentId ->
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

        paymentRepository.save(Payment.fromGovUkPay(createdGovUkPayPayment.payment, renewalDate, incompleteProperty))

        return PropertyRegistrationPaymentCreationResult.NewPayment(createdGovUkPayPayment.nextUrl)
    }

    private fun recordGovUkPayStatus(
        paymentId: String,
        govUkPayStatus: PaymentStatus,
        journeyId: String,
    ): PaymentStatus {
        if (govUkPayStatus == PaymentStatus.SUCCEEDED) {
            return requireManualIntervention(
                paymentId,
                "GOV.UK Pay reports it succeeded, but it was not finalised before creating a new payment for journey $journeyId",
            )
        }

        if (govUkPayStatus !in PaymentStatus.IN_PROGRESS_STATUSES) {
            paymentRepository.updateStatusIfCurrentStatusIn(paymentId, PaymentStatus.IN_PROGRESS_STATUSES, govUkPayStatus, Instant.now())
        }
        return getStoredPaymentStatus(paymentId)
    }

    private fun checkWithGovUkPay(payments: List<Payment>): List<CheckedPayment> =
        payments
            .sortedWith(compareBy<Payment> { it.paymentCreatedAt }.thenBy { it.paymentId })
            .map { CheckedPayment(it, getGovUkPayPaymentStatus(it.paymentId)) }

    private fun checkIndeterminatePaymentsWithGovUkPay(incompleteProperty: LandlordIncompleteProperty): List<CheckedPayment> =
        checkWithGovUkPay(
            paymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty).filter { it.status == PaymentStatus.INDETERMINATE },
        )

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

    fun getPropertyRegistrationPaymentStatus(
        journeyId: String,
        paymentReference: String,
    ): PaymentStatusCheckDataModel {
        val incompleteProperty = getIncompletePropertyForCurrentUser(journeyId)
        val referencedPayment =
            checkNotNull(paymentRepository.findByReferenceAndAssociatedIncompleteProperty(paymentReference, incompleteProperty)) {
                "No payment with reference $paymentReference found for journey $journeyId"
            }
        val indeterminatePayments = checkIndeterminatePaymentsWithGovUkPay(incompleteProperty)

        indeterminatePayments.filter { it.govUkPayStatusCheck.isFailedOrCancelled() }.forEach { (payment, govUkPayStatusCheck) ->
            paymentRepository.updateStatusIfCurrentStatusIn(
                payment.paymentId,
                listOf(PaymentStatus.INDETERMINATE),
                govUkPayStatusCheck.status,
                Instant.now(),
            )
        }

        // An INDETERMINATE payment GOV.UK Pay reports succeeded comes first, so another payment is never captured alongside it
        val recoverablePayments = indeterminatePayments.filter { it.isRecoverable }
        recoverablePayments.firstOrNull()?.let { recoverablePayment ->
            recoverablePayments.drop(1).forEach { requireManualInterventionForExtraPayment(it.payment, recoverablePayment.payment) }
            return recoverablePayment.govUkPayStatusCheck
        }

        // An INDETERMINATE payment that GOV.UK Pay still reports in progress comes before the referenced payment
        val paymentToCheck =
            indeterminatePayments.firstOrNull { it.govUkPayStatusCheck.isInProgress() }
                ?: indeterminatePayments.firstOrNull { it.payment.paymentId == referencedPayment.paymentId }
        return paymentToCheck?.govUkPayStatusCheck ?: getGovUkPayPaymentStatus(referencedPayment.paymentId)
    }

    private fun requireManualInterventionForExtraPayment(
        extraPayment: Payment,
        registeringPayment: Payment,
    ) {
        val status =
            requireManualIntervention(
                extraPayment.paymentId,
                "GOV.UK Pay reports it succeeded, but payment ${registeringPayment.paymentId} is being registered for this journey",
            )
        check(status != PaymentStatus.SUCCEEDED) {
            "Cannot register payment ${registeringPayment.paymentId}: payment ${extraPayment.paymentId} for this property has " +
                "already been registered"
        }
    }

    /** Returns the reference of the oldest INDETERMINATE payment that GOV.UK Pay reports as succeeded, without updating anything. */
    fun findRecoverablePaymentReference(journeyId: String): String? =
        checkIndeterminatePaymentsWithGovUkPay(getIncompletePropertyForCurrentUser(journeyId))
            .firstOrNull { it.isRecoverable }
            ?.payment
            ?.reference

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

            PaymentStatus.SUCCEEDED -> {
                when (val storedStatus = getStoredPaymentStatus(paymentId)) {
                    PaymentStatus.INDETERMINATE -> {
                        registerWithoutCapturing(paymentId, registrationData, "GOV.UK Pay reports it succeeded")
                    }

                    PaymentStatus.CREATED -> {
                        requireManualIntervention(paymentId, "GOV.UK Pay reports it succeeded, but it was not finalised")
                    }

                    else -> {
                        storedStatus
                    }
                }
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

            PaymentStatus.CAPTURABLE -> {
                finaliseCapturablePayment(paymentId, registrationData)
            }

            PaymentStatus.INDETERMINATE, PaymentStatus.MANUAL_INTERVENTION -> {
                throw IllegalArgumentException("Payment $paymentId cannot be finalised with $govUkPayStatus: it is not a GOV.UK Pay status")
            }
        }

    private fun finaliseCapturablePayment(
        paymentId: String,
        registrationData: PropertyRegistrationDataModel,
    ): PaymentStatus {
        recordPaymentAsIndeterminate(paymentId)

        var hasCaptured = false
        return try {
            registerPropertyForPayment(paymentId, registrationData) {
                captureAndConfirmPayment(paymentId)
                hasCaptured = true
            }
        } catch (exception: PaymentCaptureOutcomeUnknownException) {
            requireManualIntervention(paymentId, "could not confirm whether GOV.UK Pay captured it", exception)
        } catch (exception: PaymentNotCapturedException) {
            println("Failed to capture payment $paymentId, so recording its outcome: ${exception.stackTraceToString()}")
            recordUncapturedPaymentOutcome(paymentId, exception)
        } catch (exception: Exception) {
            if (hasCaptured) {
                println(
                    "GOV.UK Pay captured payment $paymentId but registering its property failed, so retrying without capturing: " +
                        exception.stackTraceToString(),
                )
                registerWithoutCapturing(paymentId, registrationData, "GOV.UK Pay captured it", exception)
            } else {
                println("Failed to finalise payment $paymentId, so cancelling it: ${exception.stackTraceToString()}")
                cancelUnfinalisedPayment(paymentId, exception)
            }
        }
    }

    private fun registerWithoutCapturing(
        paymentId: String,
        registrationData: PropertyRegistrationDataModel,
        takenReason: String,
        earlierFailure: Exception? = null,
    ): PaymentStatus =
        try {
            registerPropertyForPayment(paymentId, registrationData) {}
        } catch (failure: Exception) {
            earlierFailure?.let { failure.addSuppressed(it) }
            requireManualIntervention(paymentId, "$takenReason but its property could not be registered", failure)
        }

    private fun recordPaymentAsIndeterminate(paymentId: String) {
        paymentRepository.updateStatusIfCurrentStatusIn(
            paymentId,
            listOf(PaymentStatus.CREATED),
            PaymentStatus.INDETERMINATE,
            Instant.now(),
        )
    }

    private fun registerPropertyForPayment(
        paymentId: String,
        registrationData: PropertyRegistrationDataModel,
        capturePayment: () -> Unit,
    ): PaymentStatus =
        checkNotNull(
            transactionTemplate.execute { transactionStatus ->
                val incompleteProperty =
                    paymentRepository.findByIdOrNull(paymentId)?.associatedIncompleteProperty
                        ?: return@execute getStoredPaymentStatus(paymentId)

                val isClaimed =
                    paymentRepository.updateStatusIfCurrentStatusIn(
                        paymentId,
                        listOf(PaymentStatus.INDETERMINATE),
                        PaymentStatus.SUCCEEDED,
                        Instant.now(),
                    ) == 1
                if (!isClaimed) return@execute getStoredPaymentStatus(paymentId)

                val otherPaymentIds = getStoredPaymentIds(incompleteProperty) - paymentId
                checkNoOtherPaymentHasSucceeded(otherPaymentIds, paymentId)
                cancelInProgressPayments(otherPaymentIds, paymentId)

                val storedPayment = checkNotNull(paymentRepository.findByIdOrNull(paymentId)) { "Payment $paymentId not found" }
                val property =
                    propertyRegistrationService.registerProperty(registrationData.copy(renewalDate = storedPayment.forPeriodEnding))
                storedPayment.associateWithProperty(property)
                storedPayment.status = PaymentStatus.SUCCEEDED
                paymentRepository.saveAndFlush(storedPayment)

                // Detached here because the @MapsId on its savedJourneyState cascades a persist on flush, undoing the journey's deletion
                entityManager.detach(incompleteProperty)
                savedJourneyStateRepository.delete(incompleteProperty.savedJourneyState)
                paymentRepository.flush()

                check(!transactionStatus.isRollbackOnly) { "Transaction finalising payment $paymentId is marked for rollback" }

                // Capturing the payment can't be rolled back, so nothing else should run in this transaction after it.
                capturePayment()
                PaymentStatus.SUCCEEDED
            },
        )

    private fun captureAndConfirmPayment(paymentId: String) {
        val captureFailure =
            try {
                govUkPayClient.capturePayment(paymentId)
                return
            } catch (exception: GovUkPayException) {
                exception
            }
        val outcomeUnknownException = PaymentCaptureOutcomeUnknownException(paymentId, captureFailure)

        try {
            when (val govUkPayStatus = getGovUkPayPaymentStatusWithRetry(paymentId).status) {
                PaymentStatus.SUCCEEDED -> {
                    return
                }

                PaymentStatus.CAPTURABLE -> {
                    govUkPayClient.capturePayment(paymentId)
                    return
                }

                PaymentStatus.FAILED, PaymentStatus.CANCELLED -> {
                    throw PaymentNotCapturedException(paymentId, govUkPayStatus, captureFailure)
                }

                PaymentStatus.CREATED, PaymentStatus.INDETERMINATE, PaymentStatus.MANUAL_INTERVENTION -> {}
            }
        } catch (exception: GovUkPayException) {
            outcomeUnknownException.addSuppressed(exception)
        }

        throw outcomeUnknownException
    }

    private fun getGovUkPayPaymentStatusWithRetry(paymentId: String): PaymentStatusCheckDataModel =
        try {
            getGovUkPayPaymentStatus(paymentId)
        } catch (exception: GovUkPayException) {
            val isTransientFailure = exception.httpStatus?.is5xxServerError() ?: (exception.cause is ResourceAccessException)
            if (!isTransientFailure) throw exception

            // Wait only after a transient lookup failure; the first check should be immediate.
            try {
                Thread.sleep(paymentStatusRetryDelay)
            } catch (interruption: InterruptedException) {
                Thread.currentThread().interrupt()
                exception.addSuppressed(interruption)
                throw exception
            }

            // Try to get the payment status one more time.
            try {
                getGovUkPayPaymentStatus(paymentId)
            } catch (retryException: GovUkPayException) {
                if (retryException !== exception) retryException.addSuppressed(exception)
                throw retryException
            }
        }

    private fun getStoredPaymentIds(incompleteProperty: LandlordIncompleteProperty): List<String> =
        paymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty).map { it.paymentId }

    private fun checkNoOtherPaymentHasSucceeded(
        otherPaymentIds: List<String>,
        paymentId: String,
    ) {
        otherPaymentIds.firstOrNull { getStoredPaymentStatus(it) == PaymentStatus.SUCCEEDED }?.let { succeededPaymentId ->
            throw IllegalStateException(
                "Cannot finalise payment $paymentId: payment $succeededPaymentId for this property has already succeeded",
            )
        }
    }

    private fun cancelInProgressPayments(
        otherPaymentIds: List<String>,
        paymentId: String,
    ) {
        otherPaymentIds.filter { getStoredPaymentStatus(it) in PaymentStatus.IN_PROGRESS_STATUSES }.forEach { otherPaymentId ->
            val govUkPayStatus = cancelOrGetFinishedGovUkPayStatus(otherPaymentId, "before finalising payment $paymentId")
            check(govUkPayStatus.isFailedOrCancelled()) {
                "Cannot finalise payment $paymentId: GOV.UK Pay reports payment $otherPaymentId for this property as $govUkPayStatus"
            }
            paymentRepository.updateStatusIfCurrentStatusIn(
                otherPaymentId,
                PaymentStatus.IN_PROGRESS_STATUSES,
                PaymentStatus.CANCELLED,
                Instant.now(),
            )
        }
    }

    private fun recordUncapturedPaymentOutcome(
        paymentId: String,
        notCapturedException: PaymentNotCapturedException,
    ): PaymentStatus =
        try {
            paymentRepository.updateStatusIfCurrentStatusIn(
                paymentId,
                listOf(PaymentStatus.INDETERMINATE),
                notCapturedException.govUkPayStatus,
                Instant.now(),
            )
            getStoredPaymentStatus(paymentId)
        } catch (exception: Exception) {
            exception.addSuppressed(notCapturedException)
            throw exception
        }

    fun cancelUnfinalisedPayment(
        paymentId: String,
        cause: Exception? = null,
    ): PaymentStatus {
        val govUkPayStatus =
            try {
                cancelOrGetFinishedGovUkPayStatus(paymentId, "because it can no longer be finalised")
            } catch (cancelException: Exception) {
                cause?.let { cancelException.addSuppressed(it) }
                throw cancelException
            }

        val wasFinalisedByAnotherRequest =
            govUkPayStatus == PaymentStatus.SUCCEEDED && paymentRepository.findStatusByPaymentId(paymentId) == PaymentStatus.SUCCEEDED
        if (wasFinalisedByAnotherRequest) return PaymentStatus.SUCCEEDED

        if (!govUkPayStatus.isFailedOrCancelled()) {
            throw IllegalStateException("Payment $paymentId could not be finalised, but GOV.UK Pay reports it as $govUkPayStatus", cause)
        }

        paymentRepository.updateStatusIfCurrentStatusIn(
            paymentId,
            PaymentStatus.IN_PROGRESS_STATUSES,
            govUkPayStatus,
            Instant.now(),
        )
        return paymentRepository.findStatusByPaymentId(paymentId) ?: govUkPayStatus
    }

    private fun requireManualIntervention(
        paymentId: String,
        reason: String,
        cause: Exception? = null,
    ): PaymentStatus {
        val alert = "Payment requires manual intervention: $paymentId ($reason)"
        val isFlagged =
            try {
                paymentRepository.updateStatusIfCurrentStatusIn(
                    paymentId,
                    PaymentStatus.IN_PROGRESS_STATUSES,
                    PaymentStatus.MANUAL_INTERVENTION,
                    Instant.now(),
                ) == 1
            } catch (exception: Exception) {
                cause?.let { exception.addSuppressed(it) }
                println("$alert, but recording this failed: ${exception.stackTraceToString()}")
                throw exception
            }
        // Another request finished the payment first
        if (!isFlagged) return getStoredPaymentStatus(paymentId)

        println(if (cause == null) alert else "$alert: ${cause.stackTraceToString()}")
        emailSupportAboutManualIntervention(paymentId, reason)
        return PaymentStatus.MANUAL_INTERVENTION
    }

    private fun emailSupportAboutManualIntervention(
        paymentId: String,
        reason: String,
    ) {
        try {
            val payment = checkNotNull(paymentRepository.findByIdOrNull(paymentId)) { "Payment $paymentId not found" }
            manualInterventionEmailSender.sendEmail(
                manualInterventionEmailAddress,
                PaymentRequiresManualInterventionEmail(
                    payment.paymentId,
                    payment.reference,
                    payment.amountInPence,
                    payment.paymentCreatedAt,
                    reason,
                ),
            )
        } catch (exception: Exception) {
            println(
                "Failed to email support about payment $paymentId requiring manual intervention: " +
                    exception.stackTraceToString(),
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

    private data class CheckedPayment(
        val payment: Payment,
        val govUkPayStatusCheck: PaymentStatusCheckDataModel,
    ) {
        val isRecoverable get() = payment.status == PaymentStatus.INDETERMINATE && govUkPayStatusCheck.isSucceeded()
    }
}
