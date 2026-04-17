package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_PROPERTIES_SEARCH_PAGE
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.exceptions.RepositoryQueryTimeoutException
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.helpers.AddressHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels.PropertySearchResultViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.RegisteredPropertyLandlordViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.RegisteredPropertyLocalCouncilViewModel
import java.math.BigDecimal
import java.time.Instant

@PrsdbWebService
class PropertyOwnershipService(
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val registrationNumberService: RegistrationNumberService,
    private val localCouncilDataService: LocalCouncilDataService,
    private val licenseService: LicenseService,
    private val backLinkService: BackUrlStorageService,
) {
    @Transactional
    fun createPropertyOwnership(
        ownershipType: OwnershipType,
        numberOfHouseholds: Int,
        numberOfPeople: Int,
        primaryLandlord: Landlord,
        propertyBuildType: PropertyType,
        address: Address,
        license: License? = null,
        isActive: Boolean = true,
        numBedrooms: Int?,
        billsIncludedList: String?,
        customBillsIncluded: String?,
        furnishedStatus: FurnishedStatus?,
        rentFrequency: RentFrequency?,
        customRentFrequency: String?,
        rentAmount: BigDecimal?,
        customPropertyType: String?,
    ): PropertyOwnership {
        val registrationNumber = registrationNumberService.createRegistrationNumber(RegistrationNumberType.PROPERTY)

        return propertyOwnershipRepository.save(
            PropertyOwnership(
                ownershipType = ownershipType,
                currentNumHouseholds = numberOfHouseholds,
                currentNumTenants = numberOfPeople,
                registrationNumber = registrationNumber,
                primaryLandlord = primaryLandlord,
                propertyBuildType = propertyBuildType,
                customPropertyType = customPropertyType,
                address = address,
                license = license,
                isActive = isActive,
                numBedrooms = numBedrooms,
                billsIncludedList = billsIncludedList,
                customBillsIncluded = customBillsIncluded,
                furnishedStatus = furnishedStatus,
                rentFrequency = rentFrequency,
                customRentFrequency = customRentFrequency,
                rentAmount = rentAmount,
            ),
        )
    }

    fun getPropertyOwnershipIfAuthorizedUser(
        propertyOwnershipId: Long,
        baseUserId: String,
    ): PropertyOwnership {
        val propertyOwnership = getPropertyOwnership(propertyOwnershipId)

        val isLocalCouncil = localCouncilDataService.getIsLocalCouncilUser(baseUserId)

        val isPrimaryLandlord = propertyOwnership.primaryLandlord.baseUser.id == baseUserId

        if (!isLocalCouncil && !isPrimaryLandlord) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "The current user is not authorised to view property ownership $propertyOwnershipId",
            )
        }

        return propertyOwnership
    }

    fun getPropertyOwnership(propertyOwnershipId: Long): PropertyOwnership =
        retrievePropertyOwnershipById(propertyOwnershipId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Property ownership $propertyOwnershipId not found",
            )

    fun getIsAuthorizedToEditRecord(
        propertyOwnershipId: Long,
        baseUserId: String,
    ): Boolean = getPropertyOwnership(propertyOwnershipId).primaryLandlord.baseUser.id == baseUserId

    fun getIsPrimaryLandlord(
        propertyOwnershipId: Long,
        baseUserId: String,
    ): Boolean = getPropertyOwnership(propertyOwnershipId).primaryLandlord.baseUser.id == baseUserId

    fun getRegisteredPropertiesForLandlordUser(baseUserId: String): List<RegisteredPropertyLandlordViewModel> =
        retrieveAllActivePropertiesForLandlord(baseUserId).map { propertyOwnership ->
            RegisteredPropertyLandlordViewModel.fromPropertyOwnership(
                propertyOwnership,
                currentUrlKey = backLinkService.storeCurrentUrlReturningKey(),
            )
        }

    fun getRegisteredPropertiesForLandlord(landlordId: Long): List<RegisteredPropertyLocalCouncilViewModel> =
        propertyOwnershipRepository.findAllByPrimaryLandlord_IdAndIsActiveTrue(landlordId).map { propertyOwnership ->
            RegisteredPropertyLocalCouncilViewModel.fromPropertyOwnership(
                propertyOwnership,
                currentUrlKey = backLinkService.storeCurrentUrlReturningKey(),
            )
        }

    fun retrievePropertyOwnership(registrationNumber: Long): PropertyOwnership? =
        propertyOwnershipRepository
            .findByRegistrationNumber_Number(registrationNumber)

    fun retrievePropertyOwnershipById(propertyOwnershipId: Long): PropertyOwnership? =
        propertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnershipId)

    fun searchForProperties(
        searchTerm: String,
        localCouncilBaseUserId: String,
        restrictToLocalCouncil: Boolean = false,
        restrictToLicenses: List<LicensingType> = LicensingType.entries,
        requestedPageIndex: Int = 0,
        pageSize: Int = MAX_ENTRIES_IN_PROPERTIES_SEARCH_PAGE,
    ): Page<PropertySearchResultViewModel> {
        val prn = RegistrationNumberDataModel.parseTypeOrNull(searchTerm, RegistrationNumberType.PROPERTY)
        val uprn = AddressHelper.parseUprnOrNull(searchTerm)
        val pageRequest = PageRequest.of(requestedPageIndex, pageSize)

        val matchingProperties =
            try {
                if (prn != null) {
                    propertyOwnershipRepository.searchMatchingPRN(
                        prn.number,
                        localCouncilBaseUserId,
                        restrictToLocalCouncil,
                        restrictToLicenses,
                        pageRequest,
                    )
                } else if (uprn != null) {
                    propertyOwnershipRepository.searchMatchingUPRN(
                        uprn,
                        localCouncilBaseUserId,
                        restrictToLocalCouncil,
                        restrictToLicenses,
                        pageRequest,
                    )
                } else {
                    propertyOwnershipRepository.searchMatching(
                        searchTerm,
                        localCouncilBaseUserId,
                        restrictToLocalCouncil,
                        restrictToLicenses,
                        pageRequest,
                    )
                }
            } catch (_: QueryTimeoutException) {
                throw RepositoryQueryTimeoutException("Property search with query '$searchTerm' timed out")
            }

        return matchingProperties.map {
            PropertySearchResultViewModel.fromPropertyOwnership(
                it,
                backLinkService.storeCurrentUrlReturningKey(),
            )
        }
    }

    @Transactional
    fun updateLicensing(
        id: Long,
        licensingType: LicensingType,
        licenceNumber: String?,
    ) {
        val propertyOwnership = getPropertyOwnership(id)
        val updatedLicence =
            licenseService.updateLicence(
                propertyOwnership.license,
                licensingType,
                licenceNumber,
            )
        propertyOwnership.license = updatedLicence
        propertyOwnershipRepository.save(propertyOwnership)
    }

    @Transactional
    fun updateOwnershipType(
        id: Long,
        ownershipType: OwnershipType,
    ) {
        val propertyOwnership = getPropertyOwnership(id)
        propertyOwnership.ownershipType = ownershipType
        propertyOwnershipRepository.save(propertyOwnership)
    }

    @Transactional
    fun updateOccupancy(
        id: Long,
        numberOfHouseholds: Int,
        numberOfPeople: Int,
        numBedrooms: Int?,
        billsIncludedList: String?,
        customBillsIncluded: String?,
        furnishedStatus: FurnishedStatus?,
        rentFrequency: RentFrequency?,
        customRentFrequency: String?,
        rentAmount: BigDecimal?,
        initialLastModifiedDate: Instant,
    ) {
        val propertyOwnership = getPropertyOwnership(id)
        throwErrorIfLastModifiedDatesConflict(propertyOwnership, initialLastModifiedDate)
        propertyOwnership.currentNumHouseholds = numberOfHouseholds
        propertyOwnership.currentNumTenants = numberOfPeople
        propertyOwnership.numBedrooms = numBedrooms
        propertyOwnership.billsIncludedList = billsIncludedList
        propertyOwnership.customBillsIncluded = customBillsIncluded
        propertyOwnership.furnishedStatus = furnishedStatus
        propertyOwnership.rentFrequency = rentFrequency
        propertyOwnership.customRentFrequency = customRentFrequency
        propertyOwnership.rentAmount = rentAmount
        propertyOwnershipRepository.save(propertyOwnership)
    }

    @Transactional
    fun updateHouseholdsAndTenants(
        id: Long,
        numberOfHouseholds: Int,
        numberOfPeople: Int,
        initialLastModifiedDate: Instant,
    ) {
        val propertyOwnership = getPropertyOwnership(id)
        throwErrorIfLastModifiedDatesConflict(propertyOwnership, initialLastModifiedDate)
        propertyOwnership.currentNumHouseholds = numberOfHouseholds
        propertyOwnership.currentNumTenants = numberOfPeople
        propertyOwnershipRepository.save(propertyOwnership)
    }

    @Transactional
    fun updateBedrooms(
        id: Long,
        numberOfBedrooms: Int,
        initialLastModifiedDate: Instant,
    ) {
        val propertyOwnership = getPropertyOwnership(id)
        throwErrorIfLastModifiedDatesConflict(propertyOwnership, initialLastModifiedDate)
        propertyOwnership.numBedrooms = numberOfBedrooms
        propertyOwnershipRepository.save(propertyOwnership)
    }

    @Transactional
    fun updateRentIncludesBills(
        id: Long,
        billsIncludedList: String?,
        customBillsIncluded: String?,
        initialLastModifiedDate: Instant,
    ) {
        val propertyOwnership = getPropertyOwnership(id)
        throwErrorIfLastModifiedDatesConflict(propertyOwnership, initialLastModifiedDate)
        propertyOwnership.billsIncludedList = billsIncludedList
        propertyOwnership.customBillsIncluded = customBillsIncluded
        propertyOwnershipRepository.save(propertyOwnership)
    }

    @Transactional
    fun updateFurnishedStatus(
        id: Long,
        furnishedStatus: FurnishedStatus,
        initialLastModifiedDate: Instant,
    ) {
        val propertyOwnership = getPropertyOwnership(id)
        throwErrorIfLastModifiedDatesConflict(propertyOwnership, initialLastModifiedDate)
        propertyOwnership.furnishedStatus = furnishedStatus
        propertyOwnershipRepository.save(propertyOwnership)
    }

    @Transactional
    fun updateRentFrequencyAndAmount(
        id: Long,
        rentFrequency: RentFrequency,
        customRentFrequency: String?,
        rentAmount: BigDecimal,
        initialLastModifiedDate: Instant,
    ) {
        val propertyOwnership = getPropertyOwnership(id)
        throwErrorIfLastModifiedDatesConflict(propertyOwnership, initialLastModifiedDate)
        propertyOwnership.rentFrequency = rentFrequency
        propertyOwnership.customRentFrequency = customRentFrequency
        propertyOwnership.rentAmount = rentAmount
        propertyOwnershipRepository.save(propertyOwnership)
    }

    fun retrieveAllActivePropertiesForLandlord(baseUserId: String): List<PropertyOwnership> =
        propertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrue(baseUserId)

    fun deletePropertyOwnership(propertyOwnershipId: Long) {
        propertyOwnershipRepository.deleteById(propertyOwnershipId)
    }

    fun deletePropertyOwnerships(propertyOwnerships: List<PropertyOwnership>) {
        propertyOwnershipRepository.deleteAll(propertyOwnerships)
    }

    fun getNumberOfIncompleteCompliancesForLandlord(principalName: String): Int {
        val propertyOwnerships = retrieveAllActivePropertiesForLandlord(principalName)
        return propertyOwnerships.count { it.isOccupied && it.propertyCompliance == null }
    }

    fun getIncompleteCompliancesForLandlord(principalName: String): List<ComplianceStatusDataModel> {
        val propertyOwnerships = retrieveAllActivePropertiesForLandlord(principalName)

        return propertyOwnerships
            .filter { it.isOccupied && it.propertyCompliance == null }
            .map { ComplianceStatusDataModel.fromPropertyOwnershipWithoutCompliance(it) }
    }

    fun doesLandlordHaveRegisteredProperties(baseUserId: String): Boolean =
        propertyOwnershipRepository.existsByPrimaryLandlord_BaseUser_IdAndIsActiveTrue(baseUserId)

    private fun throwErrorIfLastModifiedDatesConflict(
        propertyOwnership: PropertyOwnership,
        initialLastModifiedDate: Instant,
    ) {
        if (propertyOwnership.getMostRecentlyUpdated() != initialLastModifiedDate) {
            throw UpdateConflictException(
                "The property ownership record has been updated since this update session started.",
            )
        }
    }
}
