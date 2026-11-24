package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.helpers.RenewalDateHelper

@PrsdbWebService
class ActivationService(
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val landlordRepository: LandlordRepository,
) {
    @Transactional
    fun reactivateProperty(
        propertyOwnershipId: Long,
        reactivatingLandlord: Landlord,
    ) {
        val propertyOwnership = getPropertyOwnership(propertyOwnershipId)
        check(!propertyOwnership.isActive) { "Property ownership $propertyOwnershipId is already active" }
        val reactivatingLandlordAnniversary =
            checkNotNull(reactivatingLandlord.anniversary) { "Reactivating landlord ${reactivatingLandlord.id} has no anniversary" }

        propertyOwnership.reactivate()
        propertyOwnership.updateRenewalDate(
            RenewalDateHelper.getRenewalDateOnReactivation(propertyOwnership.renewalDate, reactivatingLandlordAnniversary),
        )
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
