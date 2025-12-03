package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_PROPERTIES_SEARCH_PAGE
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.helpers.AddressHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyOwnershipUpdateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailBulletPointList
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyUpdateConfirmation
import uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels.PropertySearchResultViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.RegisteredPropertyLandlordViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.RegisteredPropertyLocalCouncilViewModel

@PrsdbWebService
class PropertyOwnershipService(
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val registrationNumberService: RegistrationNumberService,
    private val localCouncilDataService: LocalCouncilDataService,
    private val licenseService: LicenseService,
    private val formContextService: FormContextService,
    private val backLinkService: BackUrlStorageService,
    private val updateConfirmationEmailService: EmailNotificationService<PropertyUpdateConfirmation>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
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
    ): PropertyOwnership {
        val registrationNumber = registrationNumberService.createRegistrationNumber(RegistrationNumberType.PROPERTY)
        val incompleteComplianceForm = formContextService.createEmptyFormContext(JourneyType.PROPERTY_COMPLIANCE, primaryLandlord.baseUser)

        return propertyOwnershipRepository.save(
            PropertyOwnership(
                ownershipType = ownershipType,
                currentNumHouseholds = numberOfHouseholds,
                currentNumTenants = numberOfPeople,
                registrationNumber = registrationNumber,
                primaryLandlord = primaryLandlord,
                propertyBuildType = propertyBuildType,
                address = address,
                license = license,
                incompleteComplianceForm = incompleteComplianceForm,
                isActive = isActive,
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

        return matchingProperties.map {
            PropertySearchResultViewModel.fromPropertyOwnership(
                it,
                backLinkService.storeCurrentUrlReturningKey(),
            )
        }
    }

    @Transactional
    fun updatePropertyOwnership(
        id: Long,
        update: PropertyOwnershipUpdateModel,
        checkUpdateIsValid: () -> Unit,
    ) {
        checkUpdateIsValid()
        val propertyOwnership = getPropertyOwnership(id)
        val wasPropertyOccupied = propertyOwnership.isOccupied

        update.ownershipType?.let { propertyOwnership.ownershipType = it }
        update.numberOfHouseholds?.let { propertyOwnership.currentNumHouseholds = it }
        update.numberOfPeople?.let { propertyOwnership.currentNumTenants = it }

        if (update.isLicenceUpdatable()) {
            val updatedLicence =
                licenseService.updateLicence(
                    propertyOwnership.license,
                    update.licensingType,
                    update.licenceNumber,
                )
            propertyOwnership.license = updatedLicence
        }

        propertyOwnershipRepository.save(propertyOwnership)
        sendUpdateConfirmationEmail(propertyOwnership, update, wasPropertyOccupied)
    }

    private fun sendUpdateConfirmationEmail(
        propertyOwnership: PropertyOwnership,
        update: PropertyOwnershipUpdateModel,
        wasPropertyOccupied: Boolean,
    ) {
        val isUpdatingFromOccupiedToOccupied = update.numberOfPeople?.let { wasPropertyOccupied && it > 0 } ?: false
        val hasNumberOfHouseholdsChanged = update.numberOfHouseholds?.let { wasPropertyOccupied && it > 0 } ?: false
        val isUpdatingOccupationStatus = update.numberOfPeople != null && !isUpdatingFromOccupiedToOccupied

        val updatedBullets =
            listOfNotNull(
                if (update.ownershipType != null) "ownership type" else null,
                if (update.isLicenceUpdatable()) "licensing information" else null,
                if (isUpdatingOccupationStatus) "whether the property is occupied by tenants" else null,
                if (hasNumberOfHouseholdsChanged) "the number of households living in this property" else null,
                if (isUpdatingFromOccupiedToOccupied) "the number of people living in this property" else null,
            )

        if (!updatedBullets.isEmpty()) {
            updateConfirmationEmailService.sendEmail(
                propertyOwnership.primaryLandlord.email,
                PropertyUpdateConfirmation(
                    singleLineAddress = propertyOwnership.address.singleLineAddress,
                    registrationNumber =
                        RegistrationNumberDataModel
                            .fromRegistrationNumber(propertyOwnership.registrationNumber)
                            .toString(),
                    dashboardUrl = absoluteUrlProvider.buildLandlordDashboardUri(),
                    updatedBullets = EmailBulletPointList(updatedBullets),
                ),
            )
        }
    }

    fun retrieveAllActivePropertiesForLandlord(baseUserId: String): List<PropertyOwnership> =
        propertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrue(baseUserId)

    fun deletePropertyOwnership(propertyOwnershipId: Long) {
        propertyOwnershipRepository.deleteById(propertyOwnershipId)
    }

    fun deletePropertyOwnerships(propertyOwnerships: List<PropertyOwnership>) {
        propertyOwnershipRepository.deleteAll(propertyOwnerships)
    }

    @Transactional
    fun deleteIncompleteComplianceForm(propertyOwnershipId: Long) {
        val propertyOwnership = getPropertyOwnership(propertyOwnershipId)
        propertyOwnership.incompleteComplianceForm?.let {
            formContextService.deleteFormContext(it)
            propertyOwnership.incompleteComplianceForm = null
            propertyOwnershipRepository.save(propertyOwnership)
        }
    }

    fun getNumberOfIncompleteCompliancesForLandlord(principalName: String): Int =
        propertyOwnershipRepository
            .countByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndCurrentNumTenantsIsGreaterThanAndIncompleteComplianceFormNotNull(
                principalName,
                0,
            )

    fun getIncompleteCompliancesForLandlord(principalName: String): List<ComplianceStatusDataModel> {
        val propertyOwnerships = retrieveAllActivePropertiesForLandlord(principalName)

        return propertyOwnerships
            .filter { it.isOccupied && it.isComplianceIncomplete }
            .map { ComplianceStatusDataModel.fromIncompleteComplianceForm(it) }
    }
}
