package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.OccupancyType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationStatus
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.Property
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.viewModels.RegisteredPropertyViewModel

@Service
class PropertyOwnershipService(
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val registrationNumberService: RegistrationNumberService,
) {
    @Transactional
    fun createPropertyOwnership(
        landlordType: LandlordType,
        ownershipType: OwnershipType,
        numberOfHouseholds: Int,
        numberOfPeople: Int,
        primaryLandlord: Landlord,
        property: Property,
        license: License? = null,
        isActive: Boolean = true,
        occupancyType: OccupancyType = OccupancyType.SINGLE_FAMILY_DWELLING,
    ): PropertyOwnership {
        val registrationNumber = registrationNumberService.createRegistrationNumber(RegistrationNumberType.PROPERTY)

        return propertyOwnershipRepository.save(
            PropertyOwnership(
                isActive = isActive,
                occupancyType = occupancyType,
                landlordType = landlordType,
                ownershipType = ownershipType,
                currentNumHouseholds = numberOfHouseholds,
                currentNumTenants = numberOfPeople,
                registrationNumber = registrationNumber,
                primaryLandlord = primaryLandlord,
                property = property,
                license = license,
            ),
        )
    }

    fun getRegisteredPropertiesForLandlord(baseUserId: String): List<RegisteredPropertyViewModel> =
        retrieveAllRegisteredPropertiesForLandlord(baseUserId).map { propertyOwnership ->
            RegisteredPropertyViewModel.fromPropertyOwnership(propertyOwnership)
        }

    fun getRegisteredPropertiesForLandlord(landlordId: Long): List<RegisteredPropertyViewModel> =
        retrieveAllRegisteredPropertiesForLandlord(landlordId).map { propertyOwnership ->
            RegisteredPropertyViewModel.fromPropertyOwnership(propertyOwnership)
        }

    fun retrievePropertyOwnership(registrationNumber: Long): PropertyOwnership? =
        propertyOwnershipRepository
            .findByRegistrationNumber_Number(registrationNumber)

    private fun retrieveAllRegisteredPropertiesForLandlord(baseUserId: String): List<PropertyOwnership> =
        propertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndProperty_Status(
            baseUserId,
            RegistrationStatus.REGISTERED,
        )

    private fun retrieveAllRegisteredPropertiesForLandlord(landlordId: Long): List<PropertyOwnership> =
        propertyOwnershipRepository.findAllByPrimaryLandlord_IdAndIsActiveTrueAndProperty_Status(
            landlordId,
            RegistrationStatus.REGISTERED,
        )
}
