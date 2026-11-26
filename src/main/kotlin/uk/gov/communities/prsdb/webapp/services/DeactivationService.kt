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
import uk.gov.communities.prsdb.webapp.constants.enums.JointLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordDeactivationReason
import uk.gov.communities.prsdb.webapp.constants.enums.ReactivationTaskType
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.TransactionHelper
import java.time.LocalDate

@Service
class DeactivationService(
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val landlordRepository: LandlordRepository,
    private val lettingAgentAccessService: LettingAgentAccessService,
    private val deactivationEmailService: DeactivationEmailService,
    private val jointLandlordInvitationRepository: JointLandlordInvitationRepository,
    private val transactionTemplate: TransactionTemplate,
    // TODO PDJB-2149: Remove the feature flag manager when the ACTIVE_INACTIVE flag is removed
    private val featureFlagManager: FeatureFlagManager,
) {
    @Transactional
    fun deactivateProperty(propertyOwnershipId: Long) {
        deactivatePropertyReturningLandlordDeactivationStatuses(propertyOwnershipId)
    }

    private fun deactivatePropertyReturningLandlordDeactivationStatuses(propertyOwnershipId: Long): List<Pair<Landlord, Boolean>> {
        val propertyOwnership = getPropertyOwnership(propertyOwnershipId)
        check(propertyOwnership.isActive) { "Property ownership $propertyOwnershipId is already inactive" }

        val deactivationDate = LocalDate.now(DateTimeHelper.UK_ZONE)
        propertyOwnership.deactivate(deactivationDate)
        propertyOwnershipRepository.save(propertyOwnership)
        val lettingAgentDelegation = lettingAgentAccessService.getDelegationByPropertyOwnershipIdOrNull(propertyOwnershipId)
        lettingAgentAccessService.deleteDelegationByPropertyOwnershipId(propertyOwnershipId)
        val jointLandlordInvitations = jointLandlordInvitationRepository.findByRegisteredOwnership_Id(propertyOwnershipId)
        val pendingInviteeEmails =
            jointLandlordInvitations
                .filter { it.status == JointLandlordInvitationStatus.PENDING }
                .map { it.invitedEmail }
        jointLandlordInvitationRepository.deleteAll(jointLandlordInvitations)
        // TODO PDJB-2149: Remove the ACTIVE_INACTIVE feature flag check
        if (lettingAgentDelegation != null && featureFlagManager.checkFeature(ACTIVE_INACTIVE)) {
            TransactionHelper.runAfterTransactionCommits {
                deactivationEmailService.sendLettingAgentPropertyDeactivatedEmail(propertyOwnership, lettingAgentDelegation.invitedEmail)
            }
        }
        // TODO PDJB-2149: Remove the ACTIVE_INACTIVE feature flag check
        if (pendingInviteeEmails.isNotEmpty() && featureFlagManager.checkFeature(ACTIVE_INACTIVE)) {
            TransactionHelper.runAfterTransactionCommits {
                pendingInviteeEmails.forEach { deactivationEmailService.sendInviteeCancellationEmail(propertyOwnership, it) }
            }
        }

        return propertyOwnership.landlords.map { landlord ->
            val deactivated =
                if (landlordHasOtherActiveProperties(landlord.id, propertyOwnershipId)) {
                    false
                } else {
                    markLandlordAsInactive(landlord, deactivationDate)
                    true
                }
            landlord to deactivated
        }
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
                transactionTemplate.executeWithoutResult {
                    val property = getPropertyOwnership(propertyOwnershipId)
                    val deactivationReasons = ReactivationTaskType.requiredFor(property)
                    if (deactivationReasons.isNotEmpty()) {
                        val landlordStatuses = deactivatePropertyReturningLandlordDeactivationStatuses(property.id)
                        // TODO PDJB-2149: Remove the ACTIVE_INACTIVE feature flag check
                        if (featureFlagManager.checkFeature(ACTIVE_INACTIVE)) {
                            TransactionHelper.runAfterTransactionCommits {
                                deactivationEmailService.sendPropertyDeactivatedEmail(property, landlordStatuses, deactivationReasons)
                            }
                        }
                    }
                }
            } catch (exception: Exception) {
                logger.error("Failed to deactivate property ownership $propertyOwnershipId", exception)
                failureCount++
            }
        }

        return failureCount
    }

    fun getIdsOfLandlordsCreatedBeforeDeactivationCutoffWhoNeverRegisteredAProperty(): List<Long> {
        val cutoff =
            LocalDate
                .now(DateTimeHelper.UK_ZONE)
                .minusMonths(LANDLORD_WITHOUT_PROPERTY_DEACTIVATION_PERIOD_IN_MONTHS.toLong())
                .atStartOfDay(DateTimeHelper.UK_ZONE)
                .toInstant()
        return landlordRepository.findAllByIsActiveTrueAndHasRegisteredPropertyFalseAndCreatedDateBefore(cutoff).map { it.id }
    }

    fun getIdsOfLandlordsWhoPreviouslyRegisteredAPropertyButHaveNoActiveProperties(): List<Long> =
        landlordRepository.findIdsOfActiveLandlordsWithRegisteredPropertyButNoActiveProperties()

    @Transactional
    fun deactivateLandlordIfNoActiveProperties(landlordId: Long): Boolean {
        val landlord =
            landlordRepository.findById(landlordId).orElseThrow {
                IllegalStateException("Landlord $landlordId not found")
            }
        if (!landlord.isActive || propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlordId)) return false

        deactivateLandlord(landlord, LandlordDeactivationReason.NO_PROPERTIES_REGISTERED, sendEmail = true)
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
        sendEmail: Boolean,
    ) {
        check(landlord.isActive) { "Landlord ${landlord.id} is already inactive" }
        check(!propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)) {
            "Landlord ${landlord.id} cannot be deactivated while they have active properties"
        }
        markLandlordAsInactive(landlord, LocalDate.now(DateTimeHelper.UK_ZONE))

        // TODO PDJB-2149: Remove the ACTIVE_INACTIVE check when the flag is removed
        if (featureFlagManager.checkFeature(ACTIVE_INACTIVE) && sendEmail) {
            deactivationEmailService.sendLandlordDeactivatedEmail(landlord, reason)
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
