package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_PROPERTIES_SEARCH_PAGE
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
import uk.gov.communities.prsdb.webapp.helpers.AddressHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels.PropertySearchResultViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.RegisteredPropertyViewModel

@Service
class PropertyOwnershipService(
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val registrationNumberService: RegistrationNumberService,
    private val localAuthorityDataService: LocalAuthorityDataService,
) {
    @Transactional
    fun createPropertyOwnership(
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

    fun getPropertyOwnershipIfAuthorizedUser(
        propertyOwnershipId: Long,
        baseUserId: String,
    ): PropertyOwnership {
        val propertyOwnership = getPropertyOwnership(propertyOwnershipId)

        val isLocalAuthority = localAuthorityDataService.getIsLocalAuthorityUser(baseUserId)

        val isPrimaryLandlord = propertyOwnership.primaryLandlord.baseUser.id == baseUserId

        if (!isLocalAuthority && !isPrimaryLandlord) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "The current user is not authorised to view property ownership $propertyOwnershipId",
            )
        }

        return propertyOwnership
    }

    private fun getPropertyOwnership(propertyOwnershipId: Long): PropertyOwnership =
        retrievePropertyOwnershipById(propertyOwnershipId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Property ownership $propertyOwnershipId not found",
            )

    fun getIsPrimaryLandlord(
        propertyOwnershipId: Long,
        baseUserId: String,
    ): Boolean = getPropertyOwnership(propertyOwnershipId).primaryLandlord.baseUser.id == baseUserId

    fun getIsAuthorizedToDeleteRecord(
        propertyOwnershipId: Long,
        baseUserId: String,
    ): Boolean = getIsPrimaryLandlord(propertyOwnershipId, baseUserId)

    fun getRegisteredPropertiesForLandlordUser(baseUserId: String): List<RegisteredPropertyViewModel> =
        retrieveAllRegisteredPropertiesForLandlord(baseUserId).map { propertyOwnership ->
            RegisteredPropertyViewModel.fromPropertyOwnership(propertyOwnership)
        }

    fun getRegisteredPropertiesForLandlord(landlordId: Long): List<RegisteredPropertyViewModel> =
        retrieveAllRegisteredPropertiesForLandlord(landlordId).map { propertyOwnership ->
            RegisteredPropertyViewModel.fromPropertyOwnership(propertyOwnership, isLaView = true)
        }

    fun retrievePropertyOwnership(registrationNumber: Long): PropertyOwnership? =
        propertyOwnershipRepository
            .findByRegistrationNumber_Number(registrationNumber)

    fun retrievePropertyOwnershipById(propertyOwnershipId: Long): PropertyOwnership? =
        propertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnershipId)

    fun searchForProperties(
        searchTerm: String,
        laBaseUserId: String,
        restrictToLA: Boolean = false,
        restrictToLicenses: List<LicensingType> = LicensingType.entries,
        requestedPageIndex: Int = 0,
        pageSize: Int = MAX_ENTRIES_IN_PROPERTIES_SEARCH_PAGE,
    ): Page<PropertySearchResultViewModel> {
        val prn = RegistrationNumberDataModel.parseTypeOrNull(searchTerm, RegistrationNumberType.PROPERTY)
        val uprn = AddressHelper.parseUprnOrNull(searchTerm)
        val pageRequest = PageRequest.of(requestedPageIndex, pageSize)

        val matchingProperties =
            if (prn != null) {
                propertyOwnershipRepository.searchMatchingPRN(
                    prn.number,
                    laBaseUserId,
                    restrictToLA,
                    restrictToLicenses,
                    pageRequest,
                )
            } else if (uprn != null) {
                propertyOwnershipRepository.searchMatchingUPRN(
                    uprn,
                    laBaseUserId,
                    restrictToLA,
                    restrictToLicenses,
                    pageRequest,
                )
            } else {
                propertyOwnershipRepository.searchMatching(
                    searchTerm,
                    laBaseUserId,
                    restrictToLA,
                    restrictToLicenses,
                    pageRequest,
                )
            }

        return matchingProperties.map { PropertySearchResultViewModel.fromPropertyOwnership(it) }
    }

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

    fun deletePropertyOwnership(propertyOwnership: PropertyOwnership) {
        propertyOwnershipRepository.delete(propertyOwnership)
    }
}
