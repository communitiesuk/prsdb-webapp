package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION
import uk.gov.communities.prsdb.webapp.constants.LAST_PROPERTY_REACTIVATION_WAS_PAID
import uk.gov.communities.prsdb.webapp.constants.REACTIVATED_PROPERTY_OWNERSHIP_ID
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.NotificationHelper
import uk.gov.communities.prsdb.webapp.helpers.TransactionHelper.Companion.runAfterTransactionCommits
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
    // Runs its own transaction so live payments are only cancelled once the reactivation has committed
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
            saveReactivationData(claimedProperty, reactivationData)
            activationService.reactivateClaimedProperty(claimedProperty, quote.renewalDate)
            recordReactivationAfterCommit(propertyOwnershipId, landlordWillBeReactivated, wasPaid = false)
        }

        cancelLivePaymentAndLogFailures(propertyOwnershipId)
    }

    fun getLastPropertyOwnershipIdReactivatedThisSession(): Long? = session.getAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID) as? Long

    fun wasLandlordReactivatedByLastReactivation(): Boolean =
        session.getAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION) == true

    fun wasLastReactivationPaid(): Boolean = session.getAttribute(LAST_PROPERTY_REACTIVATION_WAS_PAID) == true

    // A payment left live can't be taken, because the paid path's claim would fail
    private fun cancelLivePaymentAndLogFailures(propertyOwnershipId: Long) {
        try {
            paymentReconciliationService.cancelLivePaymentForProperty(propertyOwnershipId)
        } catch (exception: Exception) {
            println(
                "Failed to cancel the live payment for reactivated property ownership $propertyOwnershipId: " +
                    exception.stackTraceToString(),
            )
        }
    }

    // The address is checked first because the claim's is_active update would itself violate the active address index
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
