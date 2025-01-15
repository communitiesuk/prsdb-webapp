package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OccupancyType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationStatus
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.Property
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.helpers.LocalAuthorityDataHelper
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.models.dataModels.RegisteredPropertyDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

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

    fun getLandlordRegisteredPropertiesDetails(baseUserId: String): MutableList<RegisteredPropertyDataModel> {
        val allActiveProperties = retrieveAllPropertiesForLandlord(baseUserId)
        val registeredProperties = mutableListOf<RegisteredPropertyDataModel>()
        for (propertyOwnership in allActiveProperties) {
            val registeredProperty =
                RegisteredPropertyDataModel(
                    address = propertyOwnership.property.address.singleLineAddress,
                    registrationNumber =
                        RegistrationNumberDataModel
                            .fromRegistrationNumber(
                                propertyOwnership.registrationNumber,
                            ).toString(),
                    localAuthorityName =
                        LocalAuthorityDataHelper
                            .getLocalAuthorityDisplayName(
                                propertyOwnership.property.address.custodianCode,
                            ),
                    propertyLicence = getLicenceTypeDisplayName(propertyOwnership.license),
                    isTenanted = MessageKeyConverter.convert(propertyOwnership.currentNumTenants > 0),
                )
            registeredProperties.add(registeredProperty)
        }
        return registeredProperties
    }

    fun retrievePropertyOwnership(id: Long): PropertyOwnership? = propertyOwnershipRepository.findByIdOrNull(id)

    private fun retrieveAllPropertiesForLandlord(baseUserId: String): List<PropertyOwnership> =
        propertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveFalseAndProperty_Status(
            baseUserId,
            RegistrationStatus.REGISTERED,
        )

    private fun getLicenceTypeDisplayName(licence: License?): String {
        val licenceType = licence?.licenseType ?: LicensingType.NO_LICENSING
        return licenceType.displayName
    }
}
