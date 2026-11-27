package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.exceptions.PropertyAlreadyReactivatedException
import java.time.LocalDate

@PrsdbWebService
class ActivationService(
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val landlordRepository: LandlordRepository,
    private val entityManager: EntityManager,
) {
    @Transactional(Transactional.TxType.MANDATORY)
    fun claimPropertyForReactivation(propertyOwnershipId: Long): PropertyOwnership {
        if (propertyOwnershipRepository.claimForReactivation(propertyOwnershipId) == 0) {
            throw PropertyAlreadyReactivatedException(propertyOwnershipId)
        }
        return getPropertyOwnership(propertyOwnershipId).also { entityManager.refresh(it) }
    }

    @Transactional(Transactional.TxType.MANDATORY)
    fun reactivateClaimedProperty(
        propertyOwnership: PropertyOwnership,
        renewalDate: LocalDate,
        paidForUntil: LocalDate? = null,
    ) {
        check(propertyOwnership.isActive) { "Property ownership ${propertyOwnership.id} must be claimed before it is reactivated" }
        propertyOwnership.reactivate()
        propertyOwnership.updateRenewalDate(renewalDate)
        paidForUntil?.let { propertyOwnership.updatePaidForUntil(it) }
        propertyOwnershipRepository.save(propertyOwnership)

        propertyOwnership.landlords.forEach { reactivateLandlordIfInactive(it) }
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

    private fun getPropertyOwnership(propertyOwnershipId: Long): PropertyOwnership =
        propertyOwnershipRepository.findById(propertyOwnershipId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Property ownership $propertyOwnershipId not found")
        }
}
