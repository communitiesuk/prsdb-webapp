package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinInstant
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_FORM_CONTEXTS_DELETED_THIS_SESSION
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
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertiesDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import java.time.Instant

interface PropertyRegistrationService {
    fun registerProperty(
        addressModel: AddressDataModel,
        propertyType: PropertyType,
        licenseType: LicensingType,
        licenceNumber: String,
        ownershipType: OwnershipType,
        numberOfHouseholds: Int,
        numberOfPeople: Int,
        baseUserId: String,
    ): RegistrationNumber
}

interface IncompletePropertyService {
    fun getIncompletePropertiesForLandlord(principalName: String): List<IncompletePropertiesDataModel>

    fun deleteIncompleteProperty(
        incompletePropertyId: Long,
        principalName: String,
    )

    fun getAddressData(
        incompletePropertyId: Long,
        principalName: String,
    ): AddressDataModel

    fun isIncompletePropertyAvailable(incompletePropertyId: Long): Boolean
}

interface PropertyRegistrationConfirmationService {
    fun addIncompletePropertyFormContextsDeletedThisSession(formContextId: Long)

    fun getIncompletePropertyWasDeletedThisSession(contextId: Long): Boolean

    fun setLastPrnRegisteredThisSession(prn: Long)

    fun getLastPrnRegisteredThisSession(): Long?
}

@PrsdbWebService
class PropertyRegistrationMonolithicService(
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val landlordRepository: LandlordRepository,
    private val formContextRepository: FormContextRepository,
    private val registeredAddressCache: RegisteredAddressCache,
    private val addressService: AddressService,
    private val licenseService: LicenseService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val session: HttpSession,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val confirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>,
) : PropertyRegistrationService,
    IncompletePropertyService,
    PropertyRegistrationConfirmationService {
    fun getIsAddressRegistered(
        uprn: Long,
        ignoreCache: Boolean = false,
    ): Boolean {
        if (!ignoreCache) {
            val cachedResult = registeredAddressCache.getCachedAddressRegisteredResult(uprn)
            if (cachedResult != null) return cachedResult
        }

        val databaseResult = propertyOwnershipRepository.existsByIsActiveTrueAndAddress_Uprn(uprn)
        registeredAddressCache.setCachedAddressRegisteredResult(uprn, databaseResult)
        return databaseResult
    }

    @Transactional
    override fun registerProperty(
        addressModel: AddressDataModel,
        propertyType: PropertyType,
        licenseType: LicensingType,
        licenceNumber: String,
        ownershipType: OwnershipType,
        numberOfHouseholds: Int,
        numberOfPeople: Int,
        baseUserId: String,
    ): RegistrationNumber {
        if (addressModel.uprn != null && getIsAddressRegistered(addressModel.uprn, ignoreCache = true)) {
            throw EntityExistsException("Address already registered")
        }

        val address = addressService.findOrCreateAddress(addressModel)

        val landlord =
            landlordRepository.findByBaseUser_Id(baseUserId)
                ?: throw EntityNotFoundException("User not registered as a landlord")

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
                propertyBuildType = propertyType,
                address = address,
                license = license,
            )

        addressModel.uprn?.let { registeredAddressCache.setCachedAddressRegisteredResult(it, true) }

        setLastPrnRegisteredThisSession(propertyOwnership.registrationNumber.number)

        confirmationEmailSender.sendEmail(
            landlord.email,
            PropertyRegistrationConfirmationEmail(
                RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
                addressModel.singleLineAddress,
                absoluteUrlProvider.buildLandlordDashboardUri().toString(),
                propertyOwnership.currentNumTenants > 0,
            ),
        )

        return propertyOwnership.registrationNumber
    }

    override fun setLastPrnRegisteredThisSession(prn: Long) = session.setAttribute(PROPERTY_REGISTRATION_NUMBER, prn)

    override fun getLastPrnRegisteredThisSession() = session.getAttribute(PROPERTY_REGISTRATION_NUMBER)?.toString()?.toLong()

    override fun getIncompletePropertiesForLandlord(principalName: String): List<IncompletePropertiesDataModel> {
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
        val address = formContext.toAddressData()

        return IncompletePropertiesDataModel(
            contextId = formContext.id,
            completeByDate = completeByDate,
            singleLineAddress = address.singleLineAddress,
        )
    }

    private fun getIncompletePropertyCompleteByDate(createdDate: Instant): LocalDate {
        val createdDateInUk = DateTimeHelper.getDateInUK(createdDate.toKotlinInstant())
        return DateTimeHelper.get28DaysFromDate(createdDateInUk)
    }

    override fun getAddressData(
        incompletePropertyId: Long,
        principalName: String,
    ): AddressDataModel {
        val incompletePropertyFormContext = getIncompletePropertyFormContextForLandlordIfNotExpired(incompletePropertyId, principalName)
        return incompletePropertyFormContext.toAddressData()
    }

    private fun FormContext.toAddressData(): AddressDataModel = PropertyRegistrationJourneyDataHelper.getAddress(this.toJourneyData())!!

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

    override fun deleteIncompleteProperty(
        incompletePropertyId: Long,
        principalName: String,
    ) {
        val formContext = getIncompletePropertyFormContextForLandlordOrThrowNotFound(incompletePropertyId, principalName)
        formContextRepository.delete(formContext)
    }

    override fun addIncompletePropertyFormContextsDeletedThisSession(formContextId: Long) {
        session.setAttribute(
            INCOMPLETE_PROPERTY_FORM_CONTEXTS_DELETED_THIS_SESSION,
            getIncompletePropertyFormContextsDeletedThisSession().plus(formContextId),
        )
    }

    override fun getIncompletePropertyWasDeletedThisSession(contextId: Long): Boolean =
        getIncompletePropertyFormContextsDeletedThisSession().contains(contextId)

    private fun getIncompletePropertyFormContextsDeletedThisSession(): MutableList<Long> =
        session.getAttribute(INCOMPLETE_PROPERTY_FORM_CONTEXTS_DELETED_THIS_SESSION) as MutableList<Long>? ?: mutableListOf()

    override fun isIncompletePropertyAvailable(incompletePropertyId: Long): Boolean =
        formContextRepository.findById(incompletePropertyId).isPresent
}
