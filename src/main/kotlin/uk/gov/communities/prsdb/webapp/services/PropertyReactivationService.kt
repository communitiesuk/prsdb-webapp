package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION
import uk.gov.communities.prsdb.webapp.constants.LAST_PROPERTY_REACTIVATION_WAS_PAID
import uk.gov.communities.prsdb.webapp.constants.REACTIVATED_PROPERTY_OWNERSHIP_ID
import uk.gov.communities.prsdb.webapp.database.entity.Payment
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.exceptions.PaymentQuoteExpiredException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.NotificationHelper
import uk.gov.communities.prsdb.webapp.helpers.TransactionHelper.Companion.runAfterTransactionCommits
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyPaymentQuote
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel
import java.time.LocalDate

@PrsdbWebService
class PropertyReactivationService(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyComplianceService: PropertyComplianceService,
    private val activationService: ActivationService,
    private val addressAvailabilityService: AddressAvailabilityService,
    private val landlordActiveStatusService: LandlordActiveStatusService,
    private val propertyReactivationEmailService: PropertyReactivationEmailService,
    private val session: HttpSession,
    private val userToLandlordService: UserToLandlordService,
    private val paymentService: PaymentService,
    private val paymentReconciliationService: PaymentReconciliationService,
    private val transactionTemplate: TransactionTemplate,
) {
    // Runs its own transaction so created payments are only cancelled once the reactivation has committed
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    fun reactivatePropertyWithoutPayment(
        propertyOwnershipId: Long,
        reactivationData: PropertyReactivationDataModel,
    ) {
        transactionTemplate.executeWithoutResult {
            val propertyOwnership = propertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId)
            val landlordWillBeReactivated = landlordActiveStatusService.isCurrentLandlordInactive()
            val claimedProperty = claimPropertyIfAddressAvailable(propertyOwnership)

            val quote =
                paymentService.calculatePropertyReactivationPaymentQuote(
                    claimedProperty,
                    userToLandlordService.getCurrentLandlordForUser(),
                    LocalDate.now(DateTimeHelper.UK_ZONE),
                )
            // TODO PDJB-2076: Throw if the quote needs payment, so the free path can't add an unpaid period
            saveReactivationData(claimedProperty, reactivationData)
            activationService.reactivateClaimedProperty(claimedProperty, quote.renewalDate)
            recordReactivationAfterCommit(propertyOwnershipId, landlordWillBeReactivated, wasPaid = false)
        }

        cancelCreatedPaymentAndLogFailures(propertyOwnershipId)
    }

    @Transactional(Transactional.TxType.MANDATORY)
    fun reactivatePropertyForPayment(
        payment: Payment,
        reactivationData: PropertyReactivationDataModel,
    ) {
        val propertyOwnership = checkNotNull(payment.associatedProperty) { "Payment ${payment.paymentId} is not for a property" }
        val reactivatingLandlord = userToLandlordService.getCurrentLandlordForUser()
        if (propertyOwnership.landlords.none { it.id == reactivatingLandlord.id }) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Landlord ${reactivatingLandlord.id} is not a landlord of property ownership ${propertyOwnership.id}",
            )
        }
        val landlordWillBeReactivated = landlordActiveStatusService.isCurrentLandlordInactive()
        val claimedProperty = claimPropertyIfAddressAvailable(propertyOwnership)

        val quote =
            paymentService.calculatePropertyReactivationPaymentQuote(
                claimedProperty,
                reactivatingLandlord,
                LocalDate.now(DateTimeHelper.UK_ZONE),
            )
        checkPaymentMatchesQuote(payment, quote)
        saveReactivationData(claimedProperty, reactivationData)
        activationService.reactivateClaimedProperty(claimedProperty, payment.forPeriodEnding, paidForUntil = payment.forPeriodEnding)
        recordReactivationAfterCommit(claimedProperty.id, landlordWillBeReactivated, wasPaid = true)
    }

    fun getLastPropertyOwnershipIdReactivatedThisSession(): Long? = session.getAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID) as? Long

    fun wasLandlordReactivatedByLastReactivation(): Boolean =
        session.getAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION) == true

    fun wasLastReactivationPaid(): Boolean = session.getAttribute(LAST_PROPERTY_REACTIVATION_WAS_PAID) == true

    fun cancelCreatedPaymentAndLogFailures(propertyOwnershipId: Long) {
        try {
            paymentReconciliationService.cancelCreatedPaymentForProperty(propertyOwnershipId)
        } catch (exception: Exception) {
            println(
                "Failed to cancel the created payment for reactivated property ownership $propertyOwnershipId: " +
                    exception.stackTraceToString(),
            )
        }
    }

    private fun claimPropertyIfAddressAvailable(propertyOwnership: PropertyOwnership): PropertyOwnership {
        val uprn = propertyOwnership.address.uprn
        if (uprn != null && addressAvailabilityService.hasAnotherActiveRegistration(uprn, propertyOwnership.id)) {
            throw EntityExistsException(
                "Cannot reactivate property ownership ${propertyOwnership.id} as its address (UPRN $uprn) " +
                    "is already registered to an active property ownership",
            )
        }
        return activationService.claimPropertyForReactivation(propertyOwnership.id)
    }

    private fun checkPaymentMatchesQuote(
        payment: Payment,
        quote: PropertyPaymentQuote,
    ) {
        if (payment.quoteDate != quote.quoteDate ||
            payment.amountInPence != quote.amountInPence ||
            payment.forPeriodEnding != quote.renewalDate
        ) {
            throw PaymentQuoteExpiredException(
                "Payment ${payment.paymentId} for ${payment.amountInPence}p until ${payment.forPeriodEnding}, quoted on " +
                    "${payment.quoteDate}, doesn't match the ${quote.quoteDate} quote of ${quote.amountInPence}p " +
                    "until ${quote.renewalDate}",
            )
        }
    }

    private fun recordReactivationAfterCommit(
        propertyOwnershipId: Long,
        landlordWillBeReactivated: Boolean,
        wasPaid: Boolean,
    ) {
        runAfterTransactionCommits {
            session.setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
            session.setAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION, landlordWillBeReactivated)
            session.setAttribute(LAST_PROPERTY_REACTIVATION_WAS_PAID, wasPaid)

            NotificationHelper.sendNotificationAndLogFailures(
                "the property reactivation confirmation email for property ownership $propertyOwnershipId",
            ) {
                propertyReactivationEmailService.sendConfirmationEmail(propertyOwnershipId, landlordWillBeReactivated)
            }

            NotificationHelper.sendNotificationAndLogFailures(
                "the joint landlord notifications for the reactivation of property ownership $propertyOwnershipId",
            ) {
                propertyReactivationEmailService.sendJointLandlordNotifications(propertyOwnershipId)
            }
        }
    }

    private fun saveReactivationData(
        propertyOwnership: PropertyOwnership,
        reactivationData: PropertyReactivationDataModel,
    ) {
        reactivationData.licensing?.let { propertyOwnershipService.saveReactivationLicensing(propertyOwnership, it) }
        reactivationData.tenancyDetails?.let { propertyOwnershipService.saveReactivationTenancyDetails(propertyOwnership, it) }

        if (reactivationData.gasSafety != null || reactivationData.electricalSafety != null || reactivationData.epc != null) {
            propertyComplianceService.saveReactivationComplianceData(
                propertyOwnership,
                reactivationData.gasSafety,
                reactivationData.electricalSafety,
                reactivationData.epc,
            )
        }
    }
}
