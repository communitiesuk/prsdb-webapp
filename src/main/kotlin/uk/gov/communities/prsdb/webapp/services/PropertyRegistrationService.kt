package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@Service
class PropertyRegistrationService(
    private val propertyRepository: PropertyRepository,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val landlordRepository: LandlordRepository,
    private val addressDataService: AddressDataService,
    private val propertyService: PropertyService,
    private val licenseService: LicenseService,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    fun getIsAddressRegistered(
        uprn: Long,
        ignoreCache: Boolean = false,
    ): Boolean {
        if (!ignoreCache) {
            val cachedResult = addressDataService.getCachedAddressRegisteredResult(uprn)
            if (cachedResult != null) return cachedResult
        }

        val property = propertyRepository.findByAddress_Uprn(uprn)
        if (property == null || !property.isActive) {
            addressDataService.setCachedAddressRegisteredResult(uprn, false)
            return false
        }

        val databaseResult = propertyOwnershipRepository.existsByIsActiveTrueAndProperty_Id(property.id)
        addressDataService.setCachedAddressRegisteredResult(uprn, databaseResult)
        return databaseResult
    }

    @Transactional
    fun registerPropertyAndReturnOwnershipId(
        address: AddressDataModel,
        propertyType: PropertyType,
        licenseType: LicensingType,
        licenceNumber: String,
        landlordType: LandlordType,
        ownershipType: OwnershipType,
        numberOfHouseholds: Int,
        numberOfPeople: Int,
        baseUserId: String,
    ): Long? {
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
                landlordType = landlordType,
                ownershipType = ownershipType,
                numberOfHouseholds = numberOfHouseholds,
                numberOfPeople = numberOfPeople,
                primaryLandlord = landlord,
                property = property,
                license = license,
            )

        address.uprn?.let { addressDataService.setCachedAddressRegisteredResult(it, true) }

        return propertyOwnership.id
    }
}
