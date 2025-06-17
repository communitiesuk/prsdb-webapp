package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.PrsdbService
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_HAD_ACTIVE_PROPERTIES
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository

@PrsdbService
class LandlordDeregistrationService(
    private val landlordRepository: LandlordRepository,
    private val oneLoginUserRepository: OneLoginUserRepository,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyDeregistrationService: PropertyDeregistrationService,
    private val session: HttpSession,
) {
    @Transactional
    fun deregisterLandlordAndTheirProperties(baseUserId: String): List<PropertyOwnership> {
        val registeredProperties = propertyOwnershipService.retrieveAllPropertiesForLandlord(baseUserId)
        if (registeredProperties.isNotEmpty()) {
            propertyDeregistrationService.deregisterProperties(registeredProperties)
        }

        landlordRepository.deleteByBaseUser_Id(baseUserId)
        oneLoginUserRepository.deleteIfNotLocalAuthorityUser(baseUserId)

        return registeredProperties
    }

    fun addLandlordHadActivePropertiesToSession(hadActiveProperties: Boolean) =
        session.setAttribute(
            LANDLORD_HAD_ACTIVE_PROPERTIES,
            hadActiveProperties,
        )

    fun getLandlordHadActivePropertiesFromSession(): Boolean = session.getAttribute(LANDLORD_HAD_ACTIVE_PROPERTIES) == true
}
