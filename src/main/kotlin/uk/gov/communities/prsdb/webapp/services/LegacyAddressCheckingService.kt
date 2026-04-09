package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository

@PrsdbWebService
class LegacyAddressCheckingService(
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val registeredAddressCache: RegisteredAddressCache,
) {
    fun getIsAddressRegistered(
        uprn: Long,
        ignoreCache: Boolean = false,
    ): Boolean {
        if (!ignoreCache) {
            val cachedResult = registeredAddressCache.getCachedAddressRegisteredResult(uprn)
            if (cachedResult != null) return cachedResult
        }

        val databaseResult = propertyOwnershipRepository.existsByIsActiveTrueAndAddress_Uprn(uprn)
        registeredAddressCache.setCachedAddressRegisteredResult(uprn, databaseResult)
        return databaseResult
    }
}
