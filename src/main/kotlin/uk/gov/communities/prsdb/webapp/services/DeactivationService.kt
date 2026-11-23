package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_WITHOUT_PROPERTY_DEACTIVATION_PERIOD_IN_MONTHS
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordDeactivationReason
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.LandlordDeactivationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.NotificationHelper
import uk.gov.communities.prsdb.webapp.helpers.TransactionHelper
import java.time.LocalDate

@Service
class DeactivationService(
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val landlordRepository: LandlordRepository,
    private val landlordDeactivationRepository: LandlordDeactivationRepository,
    private val lettingAgentAccessService: LettingAgentAccessService,
    private val lettingAgentPropertyDeactivatedEmailService: LettingAgentPropertyDeactivatedEmailService,
    private val transactionTemplate: TransactionTemplate,
    // TODO PDJB-2149: Remove the feature flag manager when the ACTIVE_INACTIVE flag is removed
    private val featureFlagManager: FeatureFlagManager,
    private val landlordDeactivatedEmailService: LandlordDeactivatedEmailService,
) {
    // TODO PDJB-2168: Cancel pending joint landlord invites when a property is deactivated
    @Transactional
    fun deactivateProperty(propertyOwnershipId: Long) {
        val propertyOwnership = getPropertyOwnership(propertyOwnershipId)
        check(propertyOwnership.isActive) { "Property ownership $propertyOwnershipId is already inactive" }

        val deactivationDate = LocalDate.now(DateTimeHelper.UK_ZONE)
        propertyOwnership.deactivate(deactivationDate)
        propertyOwnershipRepository.save(propertyOwnership)
        val delegation = lettingAgentAccessService.getDelegationByPropertyOwnershipIdOrNull(propertyOwnershipId)
        lettingAgentAccessService.deleteDelegationByPropertyOwnershipId(propertyOwnershipId)
        // TODO PDJB-2149: Remove the ACTIVE_INACTIVE feature flag check
        if (delegation != null && featureFlagManager.checkFeature(ACTIVE_INACTIVE)) {
            TransactionHelper.runAfterTransactionCommits {
                NotificationHelper.sendNotificationAndLogFailures(
                    "the letting agent property deactivated email for property ownership $propertyOwnershipId",
                ) {
                    lettingAgentPropertyDeactivatedEmailService.sendLettingAgentPropertyDeactivatedEmail(
                        propertyOwnership,
                        delegation.invitedEmail,
                    )
                }
            }
        }

        propertyOwnership.landlords
            .filterNot { landlordHasOtherActiveProperties(it.id, propertyOwnershipId) }
            .forEach { markLandlordAsInactive(it, deactivationDate) }
    }

    fun deactivateMissingDetailPropertiesPastDueDateReturningFailureCount(): Int {
        val cutoff =
            LocalDate
                .now(DateTimeHelper.UK_ZONE)
                .minusDays(PROVIDE_LATER_DEADLINE_DAYS.toLong())
                .atStartOfDay(DateTimeHelper.UK_ZONE)
                .toInstant()
        val propertyOwnershipIds = propertyOwnershipRepository.findActivePropertyIdsWithAnyProvideLaterTaskCreatedBefore(cutoff)
        var failureCount = 0

        propertyOwnershipIds.forEach { propertyOwnershipId ->
            try {
                transactionTemplate.executeWithoutResult { deactivateProperty(propertyOwnershipId) }
            } catch (exception: Exception) {
                logger.error("Failed to deactivate property ownership $propertyOwnershipId", exception)
                failureCount++
            }
        }

        return failureCount
    }

    fun getIdsOfLandlordsWhoNeverRegisteredAProperty(): List<Long> {
        val cutoff =
            LocalDate
                .now(DateTimeHelper.UK_ZONE)
                .minusMonths(LANDLORD_WITHOUT_PROPERTY_DEACTIVATION_PERIOD_IN_MONTHS.toLong())
                .atStartOfDay(DateTimeHelper.UK_ZONE)
                .toInstant()
        return landlordDeactivationRepository.findIdsOfActiveLandlordsWithNoRegisteredPropertyCreatedBefore(cutoff)
    }

    fun getIdsOfLandlordsWithNoActiveProperties(): List<Long> =
        landlordDeactivationRepository.findIdsOfActiveLandlordsWithRegisteredPropertyButNoActiveProperties()

    // Re-checks the landlord in case they changed after the candidate queries ran. Returns whether they were deactivated.
    @Transactional
    fun deactivateLandlordIfNoActiveProperties(landlordId: Long): Boolean {
        val landlord =
            landlordRepository.findById(landlordId).orElseThrow {
                IllegalStateException("Landlord $landlordId not found")
            }
        if (!landlord.isActive || propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlordId)) return false

        // The same reason applies whether the landlord never registered a property or has no active properties left
        deactivateLandlord(landlord, LandlordDeactivationReason.NO_PROPERTIES_REGISTERED)
        return true
    }

    fun landlordHasOtherActiveProperties(
        landlordId: Long,
        excludingPropertyOwnershipId: Long,
    ): Boolean =
        propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(landlordId, excludingPropertyOwnershipId)

    @Transactional
    fun deactivateLandlord(
        landlord: Landlord,
        reason: LandlordDeactivationReason,
    ) {
        check(landlord.isActive) { "Landlord ${landlord.id} is already inactive" }
        check(!propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)) {
            "Landlord ${landlord.id} cannot be deactivated while they have active properties"
        }
        markLandlordAsInactive(landlord, LocalDate.now(DateTimeHelper.UK_ZONE))

        // TODO PDJB-2149: Remove the ACTIVE_INACTIVE check when the flag is removed
        if (featureFlagManager.checkFeature(ACTIVE_INACTIVE)) {
            landlordDeactivatedEmailService.sendLandlordDeactivatedEmail(landlord, reason)
        }
    }

    @Transactional
    fun deactivateLandlordIfNoActiveProperties(landlord: Landlord) {
        if (landlord.isActive && !propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)) {
            markLandlordAsInactive(landlord, LocalDate.now(DateTimeHelper.UK_ZONE))
        }
    }

    private fun markLandlordAsInactive(
        landlord: Landlord,
        deactivationDate: LocalDate,
    ) {
        check(landlord.isActive) { "Landlord ${landlord.id} is already inactive" }

        landlord.deactivate(deactivationDate)
        landlordRepository.save(landlord)
    }

    private fun getPropertyOwnership(propertyOwnershipId: Long): PropertyOwnership =
        propertyOwnershipRepository.findById(propertyOwnershipId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Property ownership $propertyOwnershipId not found")
        }

    companion object {
        private val logger = LoggerFactory.getLogger(DeactivationService::class.java)
    }
}
