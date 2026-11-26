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
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentManualInterventionReason
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperty
import uk.gov.communities.prsdb.webapp.database.entity.Payment
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.exceptions.PaymentCaptureNotTakenException
import uk.gov.communities.prsdb.webapp.exceptions.PaymentCaptureOutcomeUnknownException
import uk.gov.communities.prsdb.webapp.exceptions.PaymentQuoteExpiredException
import uk.gov.communities.prsdb.webapp.exceptions.PropertyAlreadyReactivatedException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.RenewalDateHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentCreationResult
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentStatusCheckDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyPaymentQuote
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatePaymentRequest
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatedPayment
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
    private val propertyOwnershipService: PropertyOwnershipService,
    private val paymentReconciliationService: PaymentReconciliationService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val messageSource: MessageSource,
    private val transactionTemplate: TransactionTemplate,
    private val entityManager: EntityManager,
) {
    private val gratisPeriodEndDate: LocalDate = LocalDate.parse(gratisPeriodEndDate)

    fun getPropertyRegistrationPaymentQuote(quoteDate: LocalDate = LocalDate.now(DateTimeHelper.UK_ZONE)): PropertyPaymentQuote {
        val anniversary = userToLandlordService.getCurrentLandlordForUser().anniversary ?: MonthDay.from(quoteDate)
        val renewalDate = RenewalDateHelper.getRenewalDate(anniversary, quoteDate)

        return PropertyPaymentQuote(
            amountInPence = calculateProRatedFeeInPence(renewalDate, quoteDate),
            quoteDate = quoteDate,
            renewalDate = renewalDate,
        )
    }

    fun getPropertyReactivationPaymentQuote(
        propertyOwnershipId: Long,
        quoteDate: LocalDate = LocalDate.now(DateTimeHelper.UK_ZONE),
    ): PropertyPaymentQuote =
        calculatePropertyReactivationPaymentQuote(
            propertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId),
            userToLandlordService.getCurrentLandlordForUser(),
            quoteDate,
        )

    fun calculatePropertyReactivationPaymentQuote(
        propertyOwnership: PropertyOwnership,
        reactivatingLandlord: Landlord,
        quoteDate: LocalDate,
    ): PropertyPaymentQuote {
        val anniversary =
            checkNotNull(reactivatingLandlord.anniversary) { "Reactivating landlord ${reactivatingLandlord.id} has no anniversary" }

        val newRenewalDate = RenewalDateHelper.getRenewalDateAlignedToAnniversary(propertyOwnership.renewalDate, anniversary, quoteDate)
        val chargeablePeriodStartDate = maxOf(quoteDate, propertyOwnership.paidForUntil)
        val isUnpaidPeriodAdded = newRenewalDate.isAfter(propertyOwnership.renewalDate) && newRenewalDate.isAfter(chargeablePeriodStartDate)

        return PropertyPaymentQuote(
            amountInPence = if (isUnpaidPeriodAdded) calculateProRatedFeeInPence(newRenewalDate, chargeablePeriodStartDate) else 0,
            quoteDate = quoteDate,
            renewalDate = newRenewalDate,
            chargeablePeriodStartDate = chargeablePeriodStartDate,
        )
    }

    fun getAnnualFeeInPence(): Int = annualFeeInPence

    fun getFirstChargeableDate(chargeablePeriodStartDate: LocalDate): LocalDate =
        maxOf(chargeablePeriodStartDate, gratisPeriodEndDate.plusDays(1))

    fun createPropertyRegistrationPayment(
        journeyId: String,
        email: String,
        quote: PropertyPaymentQuote,
    ): PaymentCreationResult {
        val incompleteProperty = getIncompletePropertyForCurrentUser(journeyId)

        val renewalDate = quote.renewalDate
        val amountInPence = quote.amountInPence
        check(amountInPence > 0) {
            "Cannot create a GOV.UK Pay payment for journey $journeyId: the fee for the period ending $renewalDate is " +
                "${amountInPence}p but GOV.UK Pay only accepts amounts greater than zero"
        }

        markPreviousPaymentForCancellation(incompleteProperty, journeyId)

        val reference = UUID.randomUUID().toString()
        val returnUrl = absoluteUrlProvider.buildPropertyRegistrationPaymentReturnUri(journeyId, reference).toString()
        val createdGovUkPayPayment =
            requestGovUkPayPayment(amountInPence, reference, returnUrl, "registerProperty.paymentDescription", email)

        val payment = Payment.fromGovUkPay(createdGovUkPayPayment.payment, quote.quoteDate, renewalDate, incompleteProperty)
        if (!saveUnlessAnotherPaymentStarted(payment, ONE_CREATED_PAYMENT_PER_DRAFT_CONSTRAINT, "journey $journeyId")) {
            return PaymentCreationResult.ConcurrentPaymentStarted
        }
        return PaymentCreationResult.NewPayment(createdGovUkPayPayment.nextUrl)
    }

    fun createPropertyReactivationPayment(
        propertyOwnershipId: Long,
        journeyId: String,
        email: String,
        quote: PropertyPaymentQuote,
    ): PaymentCreationResult {
        check(quote.amountInPence > 0) {
            "Cannot create a GOV.UK Pay payment to reactivate property $propertyOwnershipId: the fee for the period ending " +
                "${quote.renewalDate} is ${quote.amountInPence}p but GOV.UK Pay only accepts amounts greater than zero"
        }
        val propertyOwnership = propertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId)

        // Newest payment wins: the property's created payment is cancelled whichever landlord started it
        if (paymentReconciliationService.cancelCreatedPaymentForProperty(propertyOwnershipId) == PaymentStatus.SUCCEEDED) {
            throw PropertyAlreadyReactivatedException(propertyOwnershipId)
        }

        val reference = UUID.randomUUID().toString()
        val returnUrl = absoluteUrlProvider.buildPropertyReactivationPaymentReturnUri(propertyOwnershipId, journeyId, reference).toString()
        val createdGovUkPayPayment =
            requestGovUkPayPayment(quote.amountInPence, reference, returnUrl, "reactivateProperty.paymentDescription", email)
        val payingUser = entityManager.getReference(PrsdbUser::class.java, SecurityContextHolder.getContext().authentication!!.name)

        val payment =
            Payment.fromGovUkPayForReactivation(
                createdGovUkPayPayment.payment,
                quote.quoteDate,
                quote.renewalDate,
                propertyOwnership,
                payingUser,
            )
        if (!saveUnlessAnotherPaymentStarted(payment, ONE_CREATED_PAYMENT_PER_PROPERTY_CONSTRAINT, "property $propertyOwnershipId")) {
            return PaymentCreationResult.ConcurrentPaymentStarted
        }

        // A reactivation that committed after the first check couldn't cancel this payment, because it wasn't saved yet
        if (!propertyOwnershipService.isPropertyOwnershipInactive(propertyOwnershipId)) {
            paymentReconciliationService.markForCancellation(payment.paymentId)
            throw PropertyAlreadyReactivatedException(propertyOwnershipId)
        }
        return PaymentCreationResult.NewPayment(createdGovUkPayPayment.nextUrl)
    }

    private fun markPreviousPaymentForCancellation(
        incompleteProperty: LandlordIncompleteProperty,
        journeyId: String,
    ) {
        val previousPayment =
            paymentRepository.findByAssociatedIncompletePropertyAndStatus(incompleteProperty, PaymentStatus.CREATED) ?: return
        val statusAfterMarking = paymentReconciliationService.markForCancellation(previousPayment.paymentId)
        check(statusAfterMarking != PaymentStatus.SUCCEEDED) {
            "Cannot create a GOV.UK Pay payment for journey $journeyId: payment ${previousPayment.paymentId} for this " +
                "property has already succeeded"
        }
    }

    private fun requestGovUkPayPayment(
        amountInPence: Int,
        reference: String,
        returnUrl: String,
        descriptionKey: String,
        email: String,
    ): GovUkPayCreatedPayment =
        govUkPayClient.createPayment(
            GovUkPayCreatePaymentRequest(
                amount = amountInPence,
                reference = reference,
                description = messageSource.getMessageForKey(descriptionKey),
                returnUrl = returnUrl,
                email = email,
            ),
        )

    private fun saveUnlessAnotherPaymentStarted(
        payment: Payment,
        constraintName: String,
        paymentFor: String,
    ): Boolean {
        try {
            paymentRepository.saveAndFlush(payment)
        } catch (exception: DataIntegrityViolationException) {
            if (!exception.isViolationOf(constraintName)) throw exception
            cancelUnsavedPayment(payment.paymentId, paymentFor)
            return false
        }
        return true
    }

    private fun DataIntegrityViolationException.isViolationOf(constraintName: String): Boolean =
        generateSequence<Throwable>(this) { it.cause }
            .filterIsInstance<ConstraintViolationException>()
            .any { it.constraintName.equals(constraintName, ignoreCase = true) }

    private fun cancelUnsavedPayment(
        paymentId: String,
        paymentFor: String,
    ) {
        try {
            govUkPayClient.cancelPayment(paymentId)
        } catch (exception: GovUkPayException) {
            println(
                "Could not cancel GOV.UK Pay payment $paymentId, which was not saved because another payment was started for " +
                    "$paymentFor at the same time: ${exception.stackTraceToString()}",
            )
        }
    }

    fun getStoredPropertyRegistrationPaymentStatus(
        journeyId: String,
        paymentReference: String,
    ): PaymentStatusCheckDataModel {
        val incompleteProperty = getIncompletePropertyForCurrentUser(journeyId)
        val payment =
            checkNotNull(paymentRepository.findByReferenceAndAssociatedIncompleteProperty(paymentReference, incompleteProperty)) {
                "No payment with reference $paymentReference found for journey $journeyId"
            }
        return PaymentStatusCheckDataModel(payment.paymentId, getStoredPaymentStatus(payment.paymentId))
    }

    fun getStoredPropertyReactivationPaymentStatus(
        propertyOwnershipId: Long,
        paymentReference: String,
    ): PaymentStatusCheckDataModel {
        val payingUserId = SecurityContextHolder.getContext().authentication!!.name
        val payment =
            checkNotNull(
                paymentRepository.findByReferenceAndAssociatedProperty_IdAndPayingUser_Id(
                    paymentReference,
                    propertyOwnershipId,
                    payingUserId,
                ),
            ) { "No payment with reference $paymentReference found for property $propertyOwnershipId and user $payingUserId" }
        return PaymentStatusCheckDataModel(payment.paymentId, getStoredPaymentStatus(payment.paymentId))
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
    ): PaymentStatus = finalisePayment(paymentId, govUkPayStatus) { registerPropertyWithPayment(it, registrationData) }

    // applyPaidChanges runs before the payment is captured, so if it throws the payment is cancelled instead
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    fun finalisePayment(
        paymentId: String,
        govUkPayStatus: PaymentStatus,
        applyPaidChanges: (Payment) -> Unit,
    ): PaymentStatus =
        when (govUkPayStatus) {
            PaymentStatus.CREATED -> {
                throw IllegalArgumentException("Payment $paymentId cannot be finalised while in progress")
            }

            PaymentStatus.CAPTURABLE -> {
                finaliseCapturablePayment(paymentId, applyPaidChanges)
            }

            PaymentStatus.FAILED, PaymentStatus.CANCELLED -> {
                recordPaymentNotTaken(paymentId, govUkPayStatus)
            }

            PaymentStatus.SUCCEEDED -> {
                paymentReconciliationService.recordManualIntervention(
                    paymentId,
                    PaymentManualInterventionReason.CAPTURED_BUT_NOT_APPLIED,
                    expectedStatus = PaymentStatus.CREATED,
                )
            }

            PaymentStatus.CANCELLING, PaymentStatus.MANUAL_INTERVENTION -> {
                throw IllegalArgumentException("$govUkPayStatus is not a GOV.UK Pay status")
            }
        }

    private fun finaliseCapturablePayment(
        paymentId: String,
        applyPaidChanges: (Payment) -> Unit,
    ): PaymentStatus {
        var isCaptureAttempted = false
        var isCaptured = false
        return try {
            applyPaidChangesAndCapturePayment(
                paymentId,
                applyPaidChanges,
                onCaptureAttempted = { isCaptureAttempted = true },
                onCaptured = { isCaptured = true },
            )
        } catch (exception: PaymentQuoteExpiredException) {
            cancelPaymentForRejectedChanges(paymentId, exception)
        } catch (exception: PropertyAlreadyReactivatedException) {
            cancelPaymentForRejectedChanges(paymentId, exception)
        } catch (exception: PaymentCaptureNotTakenException) {
            println("Failed to capture payment $paymentId: ${exception.stackTraceToString()}")
            recordPaymentNotTaken(paymentId, exception.govUkPayStatus)
        } catch (exception: Exception) {
            println("Failed to finalise payment $paymentId: ${exception.stackTraceToString()}")
            when {
                isCaptured -> {
                    paymentReconciliationService.recordManualIntervention(
                        paymentId,
                        PaymentManualInterventionReason.CHANGES_DID_NOT_COMMIT,
                        expectedStatus = PaymentStatus.CREATED,
                    )
                }

                isCaptureAttempted -> {
                    paymentReconciliationService.recordManualIntervention(
                        paymentId,
                        PaymentManualInterventionReason.CAPTURE_OUTCOME_UNKNOWN,
                        expectedStatus = PaymentStatus.CREATED,
                    )
                }

                else -> {
                    paymentReconciliationService.markForCancellation(paymentId)
                }
            }
        }
    }

    private fun cancelPaymentForRejectedChanges(
        paymentId: String,
        exception: RuntimeException,
    ): PaymentStatus {
        println("Marking payment $paymentId for cancellation: ${exception.message}")
        val status = paymentReconciliationService.markForCancellation(paymentId)
        if (status == PaymentStatus.SUCCEEDED) return status else throw exception
    }

    private fun applyPaidChangesAndCapturePayment(
        paymentId: String,
        applyPaidChanges: (Payment) -> Unit,
        onCaptureAttempted: () -> Unit,
        onCaptured: () -> Unit,
    ): PaymentStatus =
        checkNotNull(
            transactionTemplate.execute { transactionStatus ->
                val isClaimed =
                    paymentRepository.updateStatusIfCurrentStatusIs(
                        paymentId,
                        PaymentStatus.CREATED,
                        PaymentStatus.SUCCEEDED,
                        Instant.now(),
                    ) == 1
                if (!isClaimed) return@execute getStoredPaymentStatus(paymentId)

                val storedPayment = checkNotNull(paymentRepository.findByIdOrNull(paymentId)) { "Payment $paymentId not found" }
                entityManager.refresh(storedPayment)
                applyPaidChanges(storedPayment)
                check(!transactionStatus.isRollbackOnly) { "Transaction finalising payment $paymentId is marked for rollback" }

                onCaptureAttempted()
                // Capturing the payment can't be rolled back, so nothing else should run in this transaction after it.
                try {
                    govUkPayClient.capturePayment(paymentId)
                } catch (captureFailure: Exception) {
                    confirmPaymentTakenAfterCaptureFailure(paymentId, captureFailure)
                }
                onCaptured()
                PaymentStatus.SUCCEEDED
            },
        )

    private fun registerPropertyWithPayment(
        storedPayment: Payment,
        registrationData: PropertyRegistrationDataModel,
    ) {
        val incompleteProperty =
            checkNotNull(storedPayment.associatedIncompleteProperty) {
                "Payment ${storedPayment.paymentId} has no incomplete property to register"
            }

        val property = propertyRegistrationService.registerProperty(registrationData.copy(renewalDate = storedPayment.forPeriodEnding))
        storedPayment.associateWithProperty(property)
        storedPayment.status = PaymentStatus.SUCCEEDED
        paymentRepository.saveAndFlush(storedPayment)

        // Detached here because the @MapsId on its savedJourneyState cascades a persist on flush, undoing the journey's deletion
        entityManager.detach(incompleteProperty)
        savedJourneyStateRepository.delete(incompleteProperty.savedJourneyState)
        paymentRepository.flush()

        checkQuoteDateMatchesRegistrationDate(storedPayment, property)
    }

    private fun confirmPaymentTakenAfterCaptureFailure(
        paymentId: String,
        captureFailure: Exception,
    ) {
        val govUkPayStatus =
            try {
                PaymentStatus.fromGovUKPayStatus(govUkPayClient.getPaymentWithRetry(paymentId).state.status)
            } catch (readFailure: Exception) {
                throw PaymentCaptureOutcomeUnknownException(
                    "Failed to get the status of payment $paymentId from GOV.UK Pay after its capture failed",
                    captureFailure,
                ).apply { addSuppressed(readFailure) }
            }

        val outcome = "GOV.UK Pay reports payment $paymentId as $govUkPayStatus after its capture failed"
        when (govUkPayStatus) {
            PaymentStatus.SUCCEEDED -> println("$outcome: ${captureFailure.stackTraceToString()}")
            PaymentStatus.FAILED, PaymentStatus.CANCELLED -> throw PaymentCaptureNotTakenException(outcome, govUkPayStatus, captureFailure)
            else -> throw PaymentCaptureOutcomeUnknownException(outcome, captureFailure)
        }
    }

    private fun recordPaymentNotTaken(
        paymentId: String,
        govUkPayStatus: PaymentStatus,
    ): PaymentStatus {
        paymentRepository.updateStatusIfCurrentStatusIs(paymentId, PaymentStatus.CREATED, govUkPayStatus, Instant.now())
        return getStoredPaymentStatus(paymentId)
    }

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
        private const val ONE_CREATED_PAYMENT_PER_PROPERTY_CONSTRAINT = "uc_payment_one_created_per_property"
    }
}
