package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_HAD_ACTIVE_PROPERTIES
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository

@PrsdbWebService
class LandlordDeregistrationService(
    private val landlordRepository: LandlordRepository,
    private val oneLoginUserRepository: OneLoginUserRepository,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyDeregistrationService: PropertyDeregistrationService,
    private val formContextService: FormContextService,
    private val formContextRepository: FormContextRepository,
    private val session: HttpSession,
) {
    @Transactional
    fun deregisterLandlordAndTheirProperties(baseUserId: String): List<PropertyOwnership> {
        val incompletePropertyRegistrations =
            formContextRepository.findAllByUser_IdAndJourneyType(
                baseUserId,
                JourneyType.PROPERTY_REGISTRATION,
            )
        if (incompletePropertyRegistrations.isNotEmpty()) {
            formContextService.deleteFormContexts(incompletePropertyRegistrations)
        }

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
