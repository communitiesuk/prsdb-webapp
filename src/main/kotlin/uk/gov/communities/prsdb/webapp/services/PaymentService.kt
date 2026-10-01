package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.clients.GovUkPayClient
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.entity.Payment
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.RenewalDateHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
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
) {
    private val gratisPeriodEndDate: LocalDate = LocalDate.parse(gratisPeriodEndDate)

    fun createPropertyRegistrationPayment(
        journeyId: String,
        returnUrl: String,
        email: String,
    ): String {
        val userId = SecurityContextHolder.getContext().authentication.name
        val incompleteProperty =
            checkNotNull(landlordIncompletePropertiesRepository.findBySavedJourneyState_JourneyIdAndUser_Id(journeyId, userId)) {
                "No incomplete property found for journey $journeyId and user $userId"
            }

        val anniversary = userToLandlordService.getCurrentLandlordForUser().anniversary ?: MonthDay.now(DateTimeHelper.UK_ZONE)
        val renewalDate = RenewalDateHelper.getRenewalDate(anniversary)
        val amountInPence = calculateProRatedFeeInPence(renewalDate)
        check(amountInPence > 0) {
            "Cannot create a GOV.UK Pay payment for journey $journeyId: the fee for the period ending $renewalDate is " +
                "${amountInPence}p but GOV.UK Pay only accepts amounts greater than zero"
        }

        paymentRepository
            .findAllByAssociatedIncompletePropertyAndStatus(incompleteProperty, PaymentStatus.CREATED)
            .forEach { cancelPayment(it) }

        val reference = UUID.randomUUID().toString()
        val createdPayment =
            govUkPayClient.createPayment(
                GovUkPayCreatePaymentRequest(
                    amount = amountInPence,
                    reference = reference,
                    description = messageSource.getMessageForKey("registerProperty.paymentDescription"),
                    returnUrl = returnUrl,
                    email = email,
                ),
            )

        paymentRepository.save(
            Payment(
                paymentId = createdPayment.paymentId,
                amountInPence = amountInPence,
                reference = reference,
                paymentCreatedAt = Instant.now(),
                forPeriodEnding = renewalDate,
                status = PaymentStatus.CREATED,
                journey = incompleteProperty,
                payingUser = incompleteProperty.user,
            ),
        )

        return createdPayment.nextUrl
    }

    private fun cancelPayment(payment: Payment) {
        val govUkPayState = govUkPayClient.getPayment(payment.paymentId).state
        if (govUkPayState.finished) {
            payment.status = PaymentStatus.fromGovUkPayStatusWhenCancelling(govUkPayState.status)
        } else {
            govUkPayClient.cancelPayment(payment.paymentId)
            payment.status = PaymentStatus.CANCELLED
        }
        paymentRepository.save(payment)
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
}
