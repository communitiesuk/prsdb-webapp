package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
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
        if (property == null || !property.isActive || property.id == null) {
            if (!ignoreCache) {
                addressDataService.setCachedAddressRegisteredResult(uprn, false)
            }
            return false
        }
        val propertyOwnership = propertyOwnershipRepository.findByIsActiveTrueAndProperty_Id(property.id)
        val databaseResult = propertyOwnership != null
        if (!ignoreCache) {
            addressDataService.setCachedAddressRegisteredResult(uprn, databaseResult)
        }

        return databaseResult
    }

    fun registerProperty(
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
        val landlord =
            landlordRepository.findByBaseUser_Id(baseUserId)
                ?: throw EntityNotFoundException("User not registered as a landlord")

        val property = propertyService.createProperty(address, propertyType)
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

        return propertyOwnership.id
    }
}
