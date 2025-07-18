package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinInstant
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_NUMBER
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertiesDataModel
import java.time.Instant

@PrsdbWebService
class PropertyRegistrationService(
    private val propertyRepository: PropertyRepository,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val landlordRepository: LandlordRepository,
    private val formContextRepository: FormContextRepository,
    private val registeredAddressCache: RegisteredAddressCache,
    private val propertyService: PropertyService,
    private val licenseService: LicenseService,
    private val localAuthorityService: LocalAuthorityService,
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

    fun getNumberOfIncompletePropertyRegistrationsForLandlord(principalName: String): Int {
        val incompleteProperties =
            formContextRepository.findAllByUser_IdAndJourneyType(principalName, JourneyType.PROPERTY_REGISTRATION)

        val filteredIncompleteProperties = filterIncompleteProperties(incompleteProperties)

        return filteredIncompleteProperties.size
    }

    private fun filterIncompleteProperties(incompleteProperties: List<FormContext>): List<FormContext> =
        incompleteProperties.filter { property ->
            val completeByDate = getIncompletePropertyCompleteByDate(property.createdDate)
            !DateTimeHelper().isDateInPast(completeByDate)
        }

    fun getIncompletePropertiesForLandlord(principalName: String): List<IncompletePropertiesDataModel> {
        val formContexts = formContextRepository.findAllByUser_IdAndJourneyType(principalName, JourneyType.PROPERTY_REGISTRATION)

        val incompleteProperties = mutableListOf<IncompletePropertiesDataModel>()

        formContexts.forEach { formContext ->
            val completeByDate = getIncompletePropertyCompleteByDate(formContext.createdDate)

            if (!DateTimeHelper().isDateInPast(completeByDate)) {
                incompleteProperties.add(getIncompletePropertiesDataModels(formContext, completeByDate))
            }
        }
        return incompleteProperties
    }

    private fun getIncompletePropertiesDataModels(
        formContext: FormContext,
        completeByDate: LocalDate,
    ): IncompletePropertiesDataModel {
        val address = getAddressData(formContext)

        val localAuthorityName = localAuthorityService.retrieveLocalAuthorityById(address.localAuthorityId!!).name

        return IncompletePropertiesDataModel(
            contextId = formContext.id,
            completeByDate = completeByDate,
            singleLineAddress = address.singleLineAddress,
            localAuthorityName = localAuthorityName,
        )
    }

    private fun getIncompletePropertyCompleteByDate(createdDate: Instant): LocalDate {
        val createdDateInUk = DateTimeHelper.getDateInUK(createdDate.toKotlinInstant())
        return DateTimeHelper.get28DaysFromDate(createdDateInUk)
    }

    fun getAddressData(formContext: FormContext): AddressDataModel {
        val formContextJourneyData = formContext.toJourneyData()
        return PropertyRegistrationJourneyDataHelper.getAddress(formContextJourneyData)!!
    }

    fun getIncompletePropertyFormContextForLandlordIfNotExpired(
        contextId: Long,
        principalName: String,
    ): FormContext {
        val formContext = getIncompletePropertyFormContextForLandlordOrThrowNotFound(contextId, principalName)
        val completeByDate = getIncompletePropertyCompleteByDate(formContext.createdDate)

        if (DateTimeHelper().isDateInPast(completeByDate)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Complete by date for form context with ID: $contextId is in the past",
            )
        }
        return formContext
    }

    private fun getIncompletePropertyFormContextForLandlordOrThrowNotFound(
        contextId: Long,
        principalName: String,
    ): FormContext =
        formContextRepository.findByIdAndUser_IdAndJourneyType(contextId, principalName, JourneyType.PROPERTY_REGISTRATION)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Form context with ID: $contextId and journey type: " +
                    "${JourneyType.PROPERTY_REGISTRATION.name} not found for base user: $principalName",
            )

    fun deleteIncompleteProperty(
        contextId: Long,
        principalName: String,
    ) {
        val formContext = getIncompletePropertyFormContextForLandlordOrThrowNotFound(contextId, principalName)
        formContextRepository.delete(formContext)
    }
}
