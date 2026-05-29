package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository

@PrsdbWebService
class AddressAvailabilityService(
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
) {
    fun isAddressOwned(uprn: Long): Boolean =
        propertyOwnershipRepository.existsByLandlordship_IsActiveTrueAndPropertyDetails_Address_Uprn(uprn)

    fun isAddressOwnedByUser(
        uprn: Long,
        userId: String,
    ): Boolean =
        propertyOwnershipRepository
            .existsByLandlordship_PrimaryLandlord_BaseUser_IdAndLandlordship_IsActiveTrueAndPropertyDetails_Address_Uprn(
                userId,
                uprn,
            )
}
