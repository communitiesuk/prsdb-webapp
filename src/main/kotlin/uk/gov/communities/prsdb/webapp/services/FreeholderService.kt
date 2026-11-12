package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.FreeholderRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel

@PrsdbWebService
class FreeholderService(
    private val freeholderRepository: FreeholderRepository,
    private val addressService: AddressService,
) {
    @Transactional
    fun createFreeholders(
        propertyOwnership: PropertyOwnership,
        freeholders: List<FreeholderDataModel>,
    ) {
        freeholders.forEach { createFreeholder(propertyOwnership, it) }
    }

    @Transactional
    fun createFreeholder(
        propertyOwnership: PropertyOwnership,
        freeholder: FreeholderDataModel,
    ) {
        freeholderRepository.save(
            Freeholder(
                propertyOwnership = propertyOwnership,
                name = freeholder.name,
                email = freeholder.email,
                address = freeholder.address?.let { addressService.findOrCreateAddress(it) },
            ),
        )
    }

    @Transactional
    fun clearFreeholders(propertyOwnership: PropertyOwnership) {
        freeholderRepository.deleteByPropertyOwnership(propertyOwnership)
    }

    // TODO PDJB-2016: remove this with the flag-off code paths, which never ask who the freeholder is.
    //  It keeps the invariant that an ownership type requiring freeholder records has at least one row.
    //  PDJB-1992 will remove the remaining flag-on caller in updateOwnershipType. The flag-off callers
    //  remain until the flag goes.
    @Transactional
    fun createPlaceholderFreeholder(propertyOwnership: PropertyOwnership) {
        freeholderRepository.save(Freeholder(propertyOwnership = propertyOwnership))
    }
}
