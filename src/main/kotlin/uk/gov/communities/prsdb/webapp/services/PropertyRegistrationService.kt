package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import java.math.BigDecimal

@PrsdbWebService
class PropertyRegistrationService(
    private val addressService: AddressService,
    private val licenseService: LicenseService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val landlordRepository: LandlordRepository,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val confirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val confirmationService: PropertyRegistrationConfirmationService,
    private val jointLandlordInvitationEmailSender: JointLandlordInvitationEmailSender,
) {
    @Transactional
    fun registerProperty(
        addressModel: AddressDataModel,
        propertyType: PropertyType,
        licenseType: LicensingType,
        licenceNumber: String,
        ownershipType: OwnershipType,
        numberOfHouseholds: Int,
        numberOfPeople: Int,
        baseUserId: String,
        numBedrooms: Int?,
        billsIncludedList: String?,
        customBillsIncluded: String?,
        furnishedStatus: FurnishedStatus?,
        rentFrequency: RentFrequency?,
        customRentFrequency: String?,
        rentAmount: BigDecimal?,
        jointLandlordEmails: List<String>? = null,
    ): RegistrationNumber {
        if (addressModel.uprn != null && propertyOwnershipRepository.existsByIsActiveTrueAndAddress_Uprn(addressModel.uprn)) {
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
                numBedrooms = numBedrooms,
                billsIncludedList = billsIncludedList,
                customBillsIncluded = customBillsIncluded,
                furnishedStatus = furnishedStatus,
                rentFrequency = rentFrequency,
                customRentFrequency = customRentFrequency,
                rentAmount = rentAmount,
                primaryLandlord = landlord,
                propertyBuildType = propertyType,
                address = address,
                license = license,
            )

        confirmationService.setLastPrnRegisteredThisSession(propertyOwnership.registrationNumber.number)

        confirmationEmailSender.sendEmail(
            landlord.email,
            PropertyRegistrationConfirmationEmail(
                RegistrationNumberDataModel.Companion
                    .fromRegistrationNumber(propertyOwnership.registrationNumber)
                    .toString(),
                addressModel.singleLineAddress,
                absoluteUrlProvider.buildLandlordDashboardUri().toString(),
                propertyOwnership.currentNumTenants > 0,
            ),
        )

        if (!jointLandlordEmails.isNullOrEmpty()) {
            jointLandlordInvitationEmailSender.sendInvitationEmails(jointLandlordEmails, propertyOwnership)
        }

        return propertyOwnership.registrationNumber
    }
}
