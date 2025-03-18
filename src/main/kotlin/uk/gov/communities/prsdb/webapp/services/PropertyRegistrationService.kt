package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DEREGISTRATION_ENTITY_IDS
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_NUMBER
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@Service
class PropertyRegistrationService(
    private val propertyRepository: PropertyRepository,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val landlordRepository: LandlordRepository,
    private val registeredAddressCache: RegisteredAddressCache,
    private val propertyService: PropertyService,
    private val licenseService: LicenseService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val session: HttpSession,
) {
    fun getIsAddressRegistered(
        uprn: Long,
        ignoreCache: Boolean = false,
    ): Boolean {
        if (!ignoreCache) {
            val cachedResult = registeredAddressCache.getCachedAddressRegisteredResult(uprn)
            if (cachedResult != null) return cachedResult
        }

        val property = propertyRepository.findByAddress_Uprn(uprn)
        if (property == null || !property.isActive) {
            registeredAddressCache.setCachedAddressRegisteredResult(uprn, false)
            return false
        }

        val databaseResult = propertyOwnershipRepository.existsByIsActiveTrueAndProperty_Id(property.id)
        registeredAddressCache.setCachedAddressRegisteredResult(uprn, databaseResult)
        return databaseResult
    }

    @Transactional
    fun registerPropertyAndReturnPropertyRegistrationNumber(
        address: AddressDataModel,
        propertyType: PropertyType,
        licenseType: LicensingType,
        licenceNumber: String,
        ownershipType: OwnershipType,
        numberOfHouseholds: Int,
        numberOfPeople: Int,
        baseUserId: String,
    ): RegistrationNumber {
        if (address.uprn != null && getIsAddressRegistered(address.uprn, ignoreCache = true)) {
            throw EntityExistsException("Address already registered")
        }

        val landlord =
            landlordRepository.findByBaseUser_Id(baseUserId)
                ?: throw EntityNotFoundException("User not registered as a landlord")

        val property = propertyService.activateOrCreateProperty(address, propertyType)

        val license =
            if (licenseType != LicensingType.NO_LICENSING) {
                licenseService.createLicense(licenseType, licenceNumber)
            } else {
                null
            }

        val propertyOwnership =
            propertyOwnershipService.createPropertyOwnership(
                ownershipType = ownershipType,
                numberOfHouseholds = numberOfHouseholds,
                numberOfPeople = numberOfPeople,
                primaryLandlord = landlord,
                property = property,
                license = license,
            )

        address.uprn?.let { registeredAddressCache.setCachedAddressRegisteredResult(it, true) }

        return propertyOwnership.registrationNumber
    }

    fun setLastPrnRegisteredThisSession(prn: Long) = session.setAttribute(PROPERTY_REGISTRATION_NUMBER, prn)

    fun getLastPrnRegisteredThisSession() = session.getAttribute(PROPERTY_REGISTRATION_NUMBER)?.toString()?.toLong()

    fun deregisterProperty(propertyOwnershipId: Long) {
        propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)?.let {
            propertyOwnershipService.deletePropertyOwnership(it)
            propertyService.deleteProperty(it.property)
        }
    }

    fun addDeregisteredPropertyAndOwnershipIdsToSession(
        propertyOwnershipId: Long,
        propertyId: Long,
    ) = session.setAttribute(
        PROPERTY_DEREGISTRATION_ENTITY_IDS,
        getDeregisteredPropertyAndOwnershipIdsFromSession().plus(Pair(propertyOwnershipId, propertyId)),
    )

    fun getDeregisteredPropertyAndOwnershipIdsFromSession(): MutableList<Pair<Long, Long>> =
        session.getAttribute(PROPERTY_DEREGISTRATION_ENTITY_IDS) as MutableList<Pair<Long, Long>>?
            ?: mutableListOf()
}
