package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
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
) {
    // TODO PDJB-2168: Cancel pending joint landlord invites when a property is deactivated
    @Transactional
    fun deactivateProperty(propertyOwnershipId: Long) {
        val propertyOwnership = getPropertyOwnership(propertyOwnershipId)
        if (!propertyOwnership.isActive) {
            println("Property ownership $propertyOwnershipId is already inactive - no change made")
            return
        }

        val deactivationDate = LocalDate.now(DateTimeHelper.UK_ZONE)
        propertyOwnership.deactivate(deactivationDate)
        propertyOwnershipRepository.save(propertyOwnership)
        // TODO PDJB-2106: Email the letting agent when their delegation is removed
        lettingAgentAccessService.deleteDelegationByPropertyOwnershipId(propertyOwnershipId)

        propertyOwnership.landlords
            .filterNot { propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(it.id, propertyOwnershipId) }
            .forEach { markLandlordAsInactive(it, deactivationDate) }
    }

    // TODO PDJB-2047: Reactivate inactive landlords when a property is reactivated
    @Transactional
    fun reactivateProperty(propertyOwnershipId: Long) {
        val propertyOwnership = getPropertyOwnership(propertyOwnershipId)
        if (propertyOwnership.isActive) {
            println("Property ownership $propertyOwnershipId is already active - no change made")
            return
        }

        propertyOwnership.reactivate()
        propertyOwnershipRepository.save(propertyOwnership)
    }

    @Transactional
    fun deactivateLandlord(landlord: Landlord) {
        check(!propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)) {
            "Landlord ${landlord.id} cannot be deactivated while they have active properties"
        }
        markLandlordAsInactive(landlord, LocalDate.now(DateTimeHelper.UK_ZONE))
    }

    @Transactional
    fun reactivateLandlord(landlord: Landlord) {
        if (landlord.isActive) {
            println("Landlord ${landlord.id} is already active - no change made")
            return
        }

        landlord.reactivate()
        landlordRepository.save(landlord)
    }

    private fun markLandlordAsInactive(
        landlord: Landlord,
        deactivationDate: LocalDate,
    ) {
        if (!landlord.isActive) {
            println("Landlord ${landlord.id} is already inactive - no change made")
            return
        }

        landlord.deactivate(deactivationDate)
        landlordRepository.save(landlord)
    }

    private fun getPropertyOwnership(propertyOwnershipId: Long): PropertyOwnership =
        propertyOwnershipRepository.findById(propertyOwnershipId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Property ownership $propertyOwnershipId not found")
        }
}
