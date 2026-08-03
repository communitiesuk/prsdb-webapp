package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository

@PrsdbWebService
class AddressAvailabilityService(
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
) {
    fun isAddressOwned(uprn: Long): Boolean = propertyOwnershipRepository.existsByIsActiveTrueAndAddress_Uprn(uprn)
}
