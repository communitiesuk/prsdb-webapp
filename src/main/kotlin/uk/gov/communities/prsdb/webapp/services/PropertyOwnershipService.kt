package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.EIC_ISSUE_DATE
import uk.gov.communities.prsdb.webapp.constants.GAS_CERTIFICATE_14_MONTH_EXPIRY
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_OUTSTANDING_ACTIONS_PAGE
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_PROPERTIES_SEARCH_PAGE
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.PropertyManager
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.exceptions.RepositoryQueryTimeoutException
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.helpers.AddressHelper
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.NotificationHelper.Companion.sendNotificationAndLogFailures
import uk.gov.communities.prsdb.webapp.helpers.RenewalDateHelper
import uk.gov.communities.prsdb.webapp.helpers.TransactionHelper.Companion.runAfterTransactionCommits
import uk.gov.communities.prsdb.webapp.helpers.converters.OwnershipTypeConverter
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.OutstandingActionsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyManagerDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels.PropertySearchResultViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.DeactivatedPropertyLandlordViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.RegisteredPropertyLandlordViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.RegisteredPropertyLocalCouncilViewModel
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.MonthDay

@PrsdbWebService
class PropertyOwnershipService(
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val registrationNumberService: RegistrationNumberService,
    private val localCouncilDataService: LocalCouncilDataService,
    private val licenseService: LicenseService,
    private val freeholderService: FreeholderService,
    private val superiorLandlordService: SuperiorLandlordService,
    private val propertyManagerService: PropertyManagerService,
    private val backLinkService: BackUrlStorageService,
    private val jointLandlordOtherLandlordLeftEmailService: JointLandlordOtherLandlordLeftEmailService,
    private val userToLandlordService: UserToLandlordService,
    private val lettingAgentAccessService: LettingAgentAccessService,
    private val lettingAgentAccessRepository: LettingAgentAccessRepository,
    private val addressService: AddressService,
    private val eicDateService: EicDateService,
    private val landlordRepository: LandlordRepository,
    private val activeInactiveService: ActiveInactiveService,
    // TODO: PDJB-1926: Remove featureFlagManager
    private val featureFlagManager: FeatureFlagManager,
) {
    // TODO PDJB-2019: stop populating ownershipTypeBeforePdjb955 and delete the legacy column.
    @Transactional
    fun createPropertyOwnership(
        ownershipType: OwnershipType,
        isOccupied: Boolean,
        numberOfHouseholds: Int,
        numberOfPeople: Int,
        registeringLandlord: Landlord,
        anniversary: MonthDay,
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
        markedJointLandlord: Boolean = false,
        licenseProvideLater: Boolean? = null,
        tenancyProvideLater: Boolean? = null,
        correspondenceEmail: String,
        correspondenceAddressModel: AddressDataModel,
        renewalDate: LocalDate = RenewalDateHelper.getRenewalDate(anniversary),
        paidForUntil: LocalDate = renewalDate,
        freeholders: List<FreeholderDataModel>? = null,
        superiorLandlords: List<SuperiorLandlordDataModel> = emptyList(),
        propertyManager: PropertyManager? = null,
    ): PropertyOwnership {
        val registrationNumber = registrationNumberService.createRegistrationNumber(RegistrationNumberType.PROPERTY)

        val correspondenceAddress = addressService.findOrCreateAddress(correspondenceAddressModel)

        registeringLandlord.markAsHavingRegisteredProperty()

        val propertyOwnership =
            propertyOwnershipRepository.save(
                PropertyOwnership(
                    ownershipType = ownershipType,
                    // TODO PDJB-2019: delete this line with the legacy column (required until then as it is not nullable)
                    ownershipTypeBeforePdjb955 = OwnershipTypeConverter.convertToBeforePdjb955(ownershipType),
                    currentNumHouseholds = numberOfHouseholds,
                    currentNumTenants = numberOfPeople,
                    isOccupied = isOccupied,
                    registrationNumber = registrationNumber,
                    landlords = mutableSetOf(registeringLandlord),
                    propertyBuildType = propertyBuildType,
                    customPropertyType = customPropertyType,
                    address = address,
                    license = license,
                    correspondenceEmail = correspondenceEmail,
                    correspondenceAddress = correspondenceAddress,
                    renewalDate = renewalDate,
                    isActive = isActive,
                    numBedrooms = numBedrooms,
                    billsIncludedList = billsIncludedList,
                    customBillsIncluded = customBillsIncluded,
                    furnishedStatus = furnishedStatus,
                    rentFrequency = rentFrequency,
                    customRentFrequency = customRentFrequency,
                    rentAmount = rentAmount,
                    markedJointLandlord = markedJointLandlord,
                    licenseProvideLater = licenseProvideLater,
                    tenancyProvideLater = tenancyProvideLater,
                    paidForUntil = paidForUntil,
                ).apply {
                    if (isOccupied) lastOccupiedDate = LocalDate.now(DateTimeHelper.UK_ZONE)
                    this.propertyManager = propertyManager
                },
            )

        if (ownershipType.requiresFreeholderRecords()) {
            when {
                // TODO PDJB-2016: delete this branch with the flag. The flag-off journey never asks who
                //  the freeholders are, so we use createPlaceholderFreeholder to supply one if required
                //  by the OwnershipType.
                freeholders == null -> {
                    freeholderService.createPlaceholderFreeholder(propertyOwnership)
                }

                freeholders.isEmpty() -> {
                    throw IllegalArgumentException(
                        "An ownership type requiring freeholder records cannot be registered with no freeholders",
                    )
                }

                else -> {
                    freeholderService.createFreeholders(propertyOwnership, freeholders)
                }
            }
        }

        // Unlike freeholders, superior landlords are not gated on the ownership type here: the caller is
        // responsible for only supplying them for ownership types that require superior landlord info.
        if (superiorLandlords.isNotEmpty()) {
            superiorLandlordService.createSuperiorLandlords(propertyOwnership, superiorLandlords)
        }

        return propertyOwnership
    }

    fun getPropertyOwnershipIfCurrentUserAuthorized(propertyOwnershipId: Long): PropertyOwnership {
        val propertyOwnership = getPropertyOwnership(propertyOwnershipId)
        val baseUserId = SecurityContextHolder.getContext().authentication!!.name
        val landlord = userToLandlordService.getCurrentLandlordForUserOrNull()

        val isLocalCouncil = localCouncilDataService.getIsLocalCouncilUser(baseUserId)

        if (isLocalCouncil) return propertyOwnership

        val isLandlord = landlord != null && propertyOwnership.landlords.any { it.id == landlord.id }

        if (isLandlord) return propertyOwnership

        throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "The current user is not authorised to view property ownership $propertyOwnershipId",
        )
    }

    fun getPropertyOwnership(propertyOwnershipId: Long): PropertyOwnership =
        retrievePropertyOwnershipById(propertyOwnershipId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Property ownership $propertyOwnershipId not found",
            )

    fun getLastModifiedDate(propertyOwnershipId: Long): Instant = getPropertyOwnership(propertyOwnershipId).getMostRecentlyUpdated()

    fun getLastModifiedDateIncludingPropertyManager(propertyOwnershipId: Long): Instant =
        getPropertyOwnership(propertyOwnershipId).getPropertyManagerMostRecentlyUpdated()

    fun getPropertyRenewalDate(propertyOwnershipId: Long): LocalDate = getPropertyOwnership(propertyOwnershipId).renewalDate

    fun getPropertyPaidForUntil(propertyOwnershipId: Long): LocalDate =
        retrievePropertyOwnershipByIdRegardlessOfActiveStatus(propertyOwnershipId).paidForUntil

    fun getLettingAgentAccess(propertyOwnershipId: Long): LettingAgentAccess? {
        if (!hasLettingAgent(propertyOwnershipId)) return null

        return lettingAgentAccessRepository.findByPropertyOwnershipId(propertyOwnershipId)
    }

    fun hasLettingAgent(propertyOwnershipId: Long): Boolean {
        val propertyOwnership = getPropertyOwnership(propertyOwnershipId)
        val lettingAgentAccess = lettingAgentAccessRepository.findByPropertyOwnershipId(propertyOwnershipId)

        return hasLettingAgent(propertyOwnership, lettingAgentAccess)
    }

    fun getCurrentUserIsAuthorizedToEditRecord(propertyOwnershipId: Long): Boolean {
        if (isCurrentUserLandlord(propertyOwnershipId)) return true

        val lettingAgentAccess = getLettingAgentAccess(propertyOwnershipId) ?: return false
        return lettingAgentAccessService.isTokenAuthorisedInSession(lettingAgentAccess.token.toString())
    }

    fun throwIfCurrentUserNotAuthorizedToEdit(propertyOwnershipId: Long) {
        if (!getCurrentUserIsAuthorizedToEditRecord(propertyOwnershipId)) {
            val baseUserId = SecurityContextHolder.getContext().authentication!!.name
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User $baseUserId is not authorized to update property ownership $propertyOwnershipId",
            )
        }
    }

    fun isCurrentUserLandlord(propertyOwnershipId: Long): Boolean {
        val landlord = userToLandlordService.getCurrentLandlordForUserOrNull() ?: return false
        return getPropertyOwnership(propertyOwnershipId).landlords.any { it.id == landlord.id }
    }

    fun getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId: Long): PropertyOwnership {
        val landlord = userToLandlordService.getCurrentLandlordForUserOrNull()
        val propertyOwnership = propertyOwnershipRepository.findByIdAndIsActiveFalse(propertyOwnershipId)

        if (landlord == null || propertyOwnership == null || propertyOwnership.landlords.none { it.id == landlord.id }) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No inactive property ownership $propertyOwnershipId found for the current landlord",
            )
        }

        return propertyOwnership
    }

    fun getActivePropertiesForLandlordUser(
        landlord: Landlord,
        currentUrlFragment: String? = null,
    ): List<RegisteredPropertyLandlordViewModel> =
        retrieveAllActivePropertiesForLandlord(landlord).map { propertyOwnership ->
            RegisteredPropertyLandlordViewModel.fromPropertyOwnership(
                propertyOwnership,
                currentUrlKey = backLinkService.storeCurrentUrlReturningKey(currentUrlFragment),
            )
        }

    fun getDeactivatedPropertiesForLandlordUser(
        landlord: Landlord,
        currentUrlFragment: String? = null,
    ): List<DeactivatedPropertyLandlordViewModel> =
        propertyOwnershipRepository
            .findAllByOwnershipLinks_Landlord_IdAndIsActiveFalse(landlord.id)
            .map { propertyOwnership ->
                DeactivatedPropertyLandlordViewModel.fromPropertyOwnership(
                    propertyOwnership,
                    currentUrlKey = backLinkService.storeCurrentUrlReturningKey(currentUrlFragment),
                )
            }

    fun getRegisteredPropertiesForLandlord(
        landlordId: Long,
        currentUrlFragment: String? = null,
    ): List<RegisteredPropertyLocalCouncilViewModel> =
        propertyOwnershipRepository
            .findAllByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlordId)
            .map { propertyOwnership ->
                RegisteredPropertyLocalCouncilViewModel.fromPropertyOwnership(
                    propertyOwnership,
                    currentUrlKey = backLinkService.storeCurrentUrlReturningKey(currentUrlFragment),
                )
            }

    fun retrievePropertyOwnership(registrationNumber: Long): PropertyOwnership? =
        propertyOwnershipRepository
            .findByRegistrationNumber_Number(registrationNumber)

    fun retrievePropertyOwnershipById(propertyOwnershipId: Long): PropertyOwnership? =
        propertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnershipId)

    private fun retrievePropertyOwnershipByIdRegardlessOfActiveStatus(propertyOwnershipId: Long): PropertyOwnership =
        propertyOwnershipRepository.findById(propertyOwnershipId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Property ownership $propertyOwnershipId not found")
        }

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
        initialLastModifiedDate: Instant,
    ) {
        val propertyOwnership = getPropertyOwnership(id)
        throwErrorIfLastModifiedDatesConflict(propertyOwnership, initialLastModifiedDate)
        val updatedLicence =
            licenseService.updateLicence(
                propertyOwnership.license,
                licensingType,
                licenceNumber,
            )
        propertyOwnership.license = updatedLicence
        propertyOwnership.licenseProvideLater = false
        propertyOwnershipRepository.save(propertyOwnership)
    }

    // TODO PDJB-2016: delete this with the flag-off journey, which never asks who the freeholders are and so falls
    //  back on a placeholder freeholder.
    @Transactional
    fun updateOwnershipType(
        id: Long,
        ownershipType: OwnershipType,
        initialLastModifiedDate: Instant,
    ) {
        val propertyOwnership = getPropertyOwnership(id)
        throwErrorIfLastModifiedDatesConflict(propertyOwnership, initialLastModifiedDate)
        propertyOwnership.ownershipType = ownershipType
        // TODO PDJB-2019: delete this line with the legacy column.
        propertyOwnership.ownershipTypeBeforePdjb955 = OwnershipTypeConverter.convertToBeforePdjb955(ownershipType)
        propertyOwnershipRepository.save(propertyOwnership)

        if (ownershipType.requiresFreeholderRecords()) {
            if (propertyOwnership.freeholders.isEmpty()) {
                freeholderService.createPlaceholderFreeholder(propertyOwnership)
            }
        } else if (propertyOwnership.freeholders.isNotEmpty()) {
            freeholderService.clearFreeholders(propertyOwnership)
        }
    }

    @Transactional
    fun updateOwnershipStructure(
        id: Long,
        ownershipType: OwnershipType,
        freeholders: List<FreeholderDataModel>,
        superiorLandlords: List<SuperiorLandlordDataModel>,
        initialLastModifiedDate: Instant,
    ): Pair<List<FreeholderDataModel>, List<SuperiorLandlordDataModel>> {
        val propertyOwnership = getPropertyOwnership(id)
        if (propertyOwnership.getOwnershipStructureMostRecentlyUpdated() != initialLastModifiedDate) {
            throw UpdateConflictException(
                "The property ownership record has been updated since this update session started.",
            )
        }
        propertyOwnership.ownershipType = ownershipType
        // TODO PDJB-2019: delete this line with the legacy column.
        propertyOwnership.ownershipTypeBeforePdjb955 = OwnershipTypeConverter.convertToBeforePdjb955(ownershipType)
        propertyOwnershipRepository.save(propertyOwnership)

        val addedFreeholders =
            if (ownershipType.requiresFreeholderRecords()) {
                require(freeholders.isNotEmpty()) { "Ownership type $ownershipType requires at least one freeholder" }
                freeholderService.updateFreeholders(propertyOwnership, freeholders)
            } else {
                freeholderService.clearFreeholders(propertyOwnership)
                emptyList()
            }

        val addedSuperiorLandlords =
            if (ownershipType.requiresSuperiorLandlordInfo() && superiorLandlords.isNotEmpty()) {
                superiorLandlordService.updateSuperiorLandlords(propertyOwnership, superiorLandlords)
            } else {
                superiorLandlordService.clearSuperiorLandlords(propertyOwnership)
                emptyList()
            }

        return Pair(addedFreeholders, addedSuperiorLandlords)
    }

    // The one-to-one has orphanRemoval, so nulling the reference deletes the property manager row.
    @Transactional
    fun removePropertyManager(
        id: Long,
        initialLastModifiedDate: Instant,
    ) {
        val propertyOwnership = getPropertyOwnership(id)
        throwErrorIfPropertyManagerLastModifiedDatesConflict(propertyOwnership, initialLastModifiedDate)
        propertyOwnership.propertyManager = null
        propertyOwnershipRepository.save(propertyOwnership)
        // TODO PDJB-2171: Email the property manager when they are removed
    }

    @Transactional
    fun updatePropertyManager(
        id: Long,
        propertyManager: PropertyManagerDataModel,
        initialLastModifiedDate: Instant,
    ) {
        val propertyOwnership = getPropertyOwnership(id)
        throwErrorIfPropertyManagerLastModifiedDatesConflict(propertyOwnership, initialLastModifiedDate)
        val existingPropertyManager = propertyOwnership.propertyManager
        if (existingPropertyManager != null) {
            propertyManagerService.updatePropertyManager(existingPropertyManager, propertyManager.name, propertyManager.email)
        } else {
            propertyOwnership.propertyManager = propertyManagerService.createPropertyManager(propertyManager.name, propertyManager.email)
        }
        propertyOwnershipRepository.save(propertyOwnership)
        // TODO PDJB-1991: Email the property manager when they are added or their details change
    }

    @Transactional
    fun updateIsOccupied(
        id: Long,
        isOccupied: Boolean,
        initialLastModifiedDate: Instant,
    ) {
        val propertyOwnership = getPropertyOwnership(id)
        throwErrorIfLastModifiedDatesConflict(propertyOwnership, initialLastModifiedDate)
        val wasOccupied = propertyOwnership.isOccupied
        propertyOwnership.isOccupied = isOccupied
        if (!wasOccupied && propertyOwnership.isOccupied) {
            // Becoming occupied defaults to "provide tenancy details later": flag the property so the record shows
            // the deadline prompt (lastOccupiedDate + 28 days) until the landlord provides the details.
            propertyOwnership.lastOccupiedDate = LocalDate.now(DateTimeHelper.UK_ZONE)
            propertyOwnership.tenancyProvideLater = true
        }
        if (!propertyOwnership.isOccupied) {
            // Tenancy details don't apply, so clear them rather than potentially holding onto stale data.
            clearTenancyDetails(propertyOwnership)
            propertyOwnership.tenancyProvideLater = null
            propertyOwnership.propertyCompliance?.apply {
                tenancyStartedBeforeEpcExpiry = null
                gasSafetyCertProvideLater = null
                electricalSafetyCertProvideLater = null
                epcProvideLater = null
            }
        }
        propertyOwnershipRepository.save(propertyOwnership)
    }

    private fun clearTenancyDetails(propertyOwnership: PropertyOwnership) {
        propertyOwnership.currentNumHouseholds = 0
        propertyOwnership.currentNumTenants = 0
        propertyOwnership.billsIncludedList = null
        propertyOwnership.customBillsIncluded = null
        propertyOwnership.furnishedStatus = null
        propertyOwnership.rentFrequency = null
        propertyOwnership.customRentFrequency = null
        propertyOwnership.rentAmount = null
    }

    @Transactional
    fun updateTenancyDetails(
        id: Long,
        numberOfHouseholds: Int,
        numberOfPeople: Int,
        billsIncludedList: String?,
        customBillsIncluded: String?,
        furnishedStatus: FurnishedStatus,
        rentFrequency: RentFrequency,
        customRentFrequency: String?,
        rentAmount: BigDecimal,
        initialLastModifiedDate: Instant,
    ) {
        val propertyOwnership = getPropertyOwnership(id)
        throwErrorIfLastModifiedDatesConflict(propertyOwnership, initialLastModifiedDate)
        propertyOwnership.currentNumHouseholds = numberOfHouseholds
        propertyOwnership.currentNumTenants = numberOfPeople
        propertyOwnership.billsIncludedList = billsIncludedList
        propertyOwnership.customBillsIncluded = customBillsIncluded
        propertyOwnership.furnishedStatus = furnishedStatus
        propertyOwnership.rentFrequency = rentFrequency
        propertyOwnership.customRentFrequency = customRentFrequency
        propertyOwnership.rentAmount = rentAmount
        // The tenancy details are now provided, so the property record no longer needs to prompt for them.
        propertyOwnership.tenancyProvideLater = false
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
    fun updateCorrespondenceEmail(
        id: Long,
        email: String,
        initialLastModifiedDate: Instant,
    ) {
        val propertyOwnership = getPropertyOwnership(id)
        throwErrorIfLastModifiedDatesConflict(propertyOwnership, initialLastModifiedDate)
        propertyOwnership.correspondenceEmail = email
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
    fun updateCorrespondenceAddress(
        id: Long,
        address: AddressDataModel,
        initialLastModifiedDate: Instant,
    ) {
        val propertyOwnership = getPropertyOwnership(id)
        throwErrorIfLastModifiedDatesConflict(propertyOwnership, initialLastModifiedDate)
        propertyOwnership.correspondenceAddress = addressService.findOrCreateAddress(address)
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

    @Transactional
    fun updatePaidForUntil(
        propertyOwnershipId: Long,
        paidForUntil: LocalDate,
    ) {
        val propertyOwnership = retrievePropertyOwnershipByIdRegardlessOfActiveStatus(propertyOwnershipId)
        propertyOwnership.updatePaidForUntil(paidForUntil)
        propertyOwnershipRepository.save(propertyOwnership)
    }

    @Transactional
    fun addLandlordToPropertyOwnership(
        propertyOwnershipId: Long,
        landlord: Landlord,
    ) {
        val propertyOwnership = getPropertyOwnership(propertyOwnershipId)
        propertyOwnership.addLandlord(landlord)
        propertyOwnershipRepository.save(propertyOwnership)
        landlord.markAsHavingRegisteredProperty()
        landlordRepository.save(landlord)
        activeInactiveService.reactivateLandlordIfInactive(landlord)
    }

    @Transactional
    fun markAsJointLandlord(propertyOwnership: PropertyOwnership) {
        propertyOwnership.markedJointLandlord = true
        propertyOwnershipRepository.save(propertyOwnership)
    }

    @Transactional
    fun markAsNotJointLandlord(propertyOwnership: PropertyOwnership) {
        propertyOwnership.markedJointLandlord = false
        propertyOwnershipRepository.save(propertyOwnership)
    }

    fun retrieveAllActivePropertiesForLandlord(landlord: Landlord): List<PropertyOwnership> =
        propertyOwnershipRepository.findAllByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)

    fun deletePropertyOwnership(propertyOwnershipId: Long) {
        propertyOwnershipRepository.deleteById(propertyOwnershipId)
    }

    fun deletePropertyOwnerships(propertyOwnerships: List<PropertyOwnership>) {
        propertyOwnershipRepository.deleteAll(propertyOwnerships)
    }

    @Transactional
    fun removeLandlord(
        propertyOwnership: PropertyOwnership,
        landlord: Landlord,
    ) {
        propertyOwnership.removeLandlord(landlord)
        propertyOwnershipRepository.save(propertyOwnership)

        runAfterTransactionCommits {
            sendNotificationAndLogFailures("the other landlord left notification emails for property ownership ${propertyOwnership.id}") {
                jointLandlordOtherLandlordLeftEmailService.sendNotificationToRemainingLandlords(propertyOwnership, landlord)
            }
        }
    }

    fun getNumberOfIncompleteCompliancesForLandlord(landlord: Landlord): Int {
        val propertyOwnerships = retrieveAllActivePropertiesForLandlord(landlord)
        return propertyOwnerships.count { it.isOccupied && it.propertyCompliance == null }
    }

    fun getOutstandingActionsForLandlord(
        landlord: Landlord,
        requestedPageIndex: Int,
    ): Page<OutstandingActionsDataModel> {
        // TODO: PDJB-1926: Remove isGas14MonthExpiryEnabled
        val isGas14MonthExpiryEnabled = featureFlagManager.checkFeature(GAS_CERTIFICATE_14_MONTH_EXPIRY)
        // TODO PDJB-1679: Remove isEicIssueDateEnabled when the EIC_ISSUE_DATE flag is removed
        val isEicIssueDateEnabled = featureFlagManager.checkFeature(EIC_ISSUE_DATE)
        val allOutstandingActions =
            retrieveAllActivePropertiesForLandlord(landlord)
                .map { propertyOwnership ->
                    val propertyCompliance = propertyOwnership.propertyCompliance
                    OutstandingActionsDataModel.fromPropertyOwnership(
                        propertyOwnership,
                        eicIssueDate = propertyCompliance?.takeIf { isEicIssueDateEnabled }?.let(eicDateService::issueDateFrom),
                        electricalSafetyExpiryDate =
                            propertyCompliance?.let {
                                PropertyComplianceService.getElectricalSafetyExpiryDate(it, eicDateService)
                            },
                        isGas14MonthExpiryEnabled = isGas14MonthExpiryEnabled,
                    )
                }.filter { it.shouldShowOnOutstandingActionsPage }
        val pageRequest = PageRequest.of(requestedPageIndex, MAX_ENTRIES_IN_OUTSTANDING_ACTIONS_PAGE)
        val fromIndex = pageRequest.offset.toInt().coerceAtMost(allOutstandingActions.size)
        val toIndex = (fromIndex + pageRequest.pageSize).coerceAtMost(allOutstandingActions.size)
        return PageImpl(allOutstandingActions.subList(fromIndex, toIndex), pageRequest, allOutstandingActions.size.toLong())
    }

    fun doesLandlordHaveRegisteredProperties(landlord: Landlord): Boolean =
        propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)

    fun doesLandlordHaveDeactivatedProperties(landlord: Landlord): Boolean =
        propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveFalse(landlord.id)

    fun getPropertyCountForLandlord(landlord: Landlord): Long = propertyOwnershipRepository.countByOwnershipLinks_Landlord_Id(landlord.id)

    private fun throwErrorIfPropertyManagerLastModifiedDatesConflict(
        propertyOwnership: PropertyOwnership,
        initialLastModifiedDate: Instant,
    ) {
        if (propertyOwnership.getPropertyManagerMostRecentlyUpdated() != initialLastModifiedDate) {
            throw UpdateConflictException(
                "The property ownership or property manager record has been updated since this update session started.",
            )
        }
    }

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

    companion object {
        // This static method allows non web services to perform this check
        @JvmStatic
        fun hasLettingAgent(
            propertyOwnership: PropertyOwnership,
            lettingAgentAccess: LettingAgentAccess?,
        ): Boolean = propertyOwnership.isOccupied && lettingAgentAccess != null
    }
}
