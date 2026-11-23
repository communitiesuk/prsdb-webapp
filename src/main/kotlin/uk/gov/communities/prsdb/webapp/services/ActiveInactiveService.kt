package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordDeactivationReason
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.time.LocalDate

@PrsdbWebService
class ActiveInactiveService(
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val landlordRepository: LandlordRepository,
    private val lettingAgentAccessService: LettingAgentAccessService,
    private val userToLandlordService: UserToLandlordService,
    private val featureFlagManager: FeatureFlagManager,
    private val landlordDeactivatedEmailService: LandlordDeactivatedEmailService,
) {
    fun isCurrentLandlordInactive(): Boolean =
        userToLandlordService.getCurrentLandlordForUserOrNull()?.let { isLandlordInactive(it) } ?: false

    // TODO PDJB-2149: Remove the ACTIVE_INACTIVE check when the flag is removed
    fun isLandlordInactive(landlord: Landlord): Boolean = featureFlagManager.checkFeature(ACTIVE_INACTIVE) && !landlord.isActive

    // TODO PDJB-2168: Cancel pending joint landlord invites when a property is deactivated
    @Transactional
    fun deactivateProperty(propertyOwnershipId: Long) {
        val propertyOwnership = getPropertyOwnership(propertyOwnershipId)
        check(propertyOwnership.isActive) { "Property ownership $propertyOwnershipId is already inactive" }

        val deactivationDate = LocalDate.now(DateTimeHelper.UK_ZONE)
        propertyOwnership.deactivate(deactivationDate)
        propertyOwnershipRepository.save(propertyOwnership)
        // TODO PDJB-2106: Email the letting agent when their delegation is removed
        lettingAgentAccessService.deleteDelegationByPropertyOwnershipId(propertyOwnershipId)

        propertyOwnership.landlords
            .filterNot { landlordHasOtherActiveProperties(it.id, propertyOwnershipId) }
            .forEach { markLandlordAsInactive(it, deactivationDate) }
    }

    fun landlordHasOtherActiveProperties(
        landlordId: Long,
        excludingPropertyOwnershipId: Long,
    ): Boolean =
        propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(landlordId, excludingPropertyOwnershipId)

    @Transactional
    fun reactivateProperty(propertyOwnershipId: Long) {
        val propertyOwnership = getPropertyOwnership(propertyOwnershipId)
        check(!propertyOwnership.isActive) { "Property ownership $propertyOwnershipId is already active" }

        propertyOwnership.reactivate()
        propertyOwnershipRepository.save(propertyOwnership)

        propertyOwnership.landlords.forEach { reactivateLandlordIfInactive(it) }
    }

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

    @Transactional
    fun reactivateLandlord(landlord: Landlord) {
        check(!landlord.isActive) { "Landlord ${landlord.id} is already active" }

        landlord.reactivate()
        landlordRepository.save(landlord)
    }

    @Transactional
    fun reactivateLandlordIfInactive(landlord: Landlord) {
        if (!landlord.isActive) reactivateLandlord(landlord)
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
}
