package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import java.math.BigDecimal
import java.time.LocalDate

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
    private val jointLandlordInvitationService: JointLandlordInvitationService,
    private val propertyComplianceService: PropertyComplianceService,
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
        customPropertyType: String?,
        jointLandlordEmails: List<String>? = null,
        hasGasSupply: Boolean? = null,
        gasSafetyCertIssueDate: LocalDate? = null,
        gasSafetyFileUploadIds: List<Long> = emptyList(),
        electricalSafetyFileUploadIds: List<Long> = emptyList(),
        electricalSafetyExpiryDate: LocalDate? = null,
        electricalCertType: CertificateType? = null,
        epcCertificateUrl: String? = null,
        epcExpiryDate: LocalDate? = null,
        epcEnergyRating: String? = null,
        tenancyStartedBeforeEpcExpiry: Boolean? = null,
        epcExemptionReason: EpcExemptionReason? = null,
        epcMeesExemptionReason: MeesExemptionReason? = null,
    ) {
        val landlord =
            landlordRepository.findByBaseUser_Id(baseUserId)
                ?: throw EntityNotFoundException("User not registered as a landlord")

        val propertyOwnership =
            createPropertyOwnershipAndRelatedEntities(
                addressModel,
                propertyType,
                licenseType,
                licenceNumber,
                ownershipType,
                numberOfHouseholds,
                numberOfPeople,
                numBedrooms,
                billsIncludedList,
                customBillsIncluded,
                furnishedStatus,
                rentFrequency,
                customRentFrequency,
                rentAmount,
                customPropertyType,
                landlord,
            )

        propertyComplianceService.saveRegistrationComplianceData(
            propertyOwnership.registrationNumber.number,
            hasGasSupply,
            gasSafetyCertIssueDate,
            gasSafetyFileUploadIds,
            electricalSafetyFileUploadIds,
            electricalSafetyExpiryDate,
            electricalCertType,
            epcCertificateUrl,
            epcExpiryDate,
            epcEnergyRating,
            tenancyStartedBeforeEpcExpiry,
            epcExemptionReason,
            epcMeesExemptionReason,
        )

        confirmationService.setLastPrnRegisteredThisSession(propertyOwnership.registrationNumber.number)

        sendConfirmationEmails(landlord, propertyOwnership, addressModel, jointLandlordEmails)
    }

    private fun createPropertyOwnershipAndRelatedEntities(
        addressModel: AddressDataModel,
        propertyType: PropertyType,
        licenseType: LicensingType,
        licenceNumber: String,
        ownershipType: OwnershipType,
        numberOfHouseholds: Int,
        numberOfPeople: Int,
        numBedrooms: Int?,
        billsIncludedList: String?,
        customBillsIncluded: String?,
        furnishedStatus: FurnishedStatus?,
        rentFrequency: RentFrequency?,
        customRentFrequency: String?,
        rentAmount: BigDecimal?,
        customPropertyType: String?,
        landlord: Landlord,
    ): PropertyOwnership {
        if (addressModel.uprn != null && propertyOwnershipRepository.existsByIsActiveTrueAndAddress_Uprn(addressModel.uprn)) {
            throw EntityExistsException("Address already registered")
        }

        val address = addressService.findOrCreateAddress(addressModel)

        val license =
            if (licenseType != LicensingType.NO_LICENSING) {
                licenseService.createLicense(licenseType, licenceNumber)
            } else {
                null
            }

        return propertyOwnershipService.createPropertyOwnership(
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
            customPropertyType = customPropertyType,
            address = address,
            license = license,
        )
    }

    private fun sendConfirmationEmails(
        landlord: Landlord,
        propertyOwnership: PropertyOwnership,
        addressModel: AddressDataModel,
        jointLandlordEmails: List<String>?,
    ) {
        confirmationEmailSender.sendEmail(
            landlord.email,
            PropertyRegistrationConfirmationEmail(
                RegistrationNumberDataModel.Companion
                    .fromRegistrationNumber(propertyOwnership.registrationNumber)
                    .toString(),
                addressModel.singleLineAddress,
                absoluteUrlProvider.buildLandlordDashboardUri().toString(),
                propertyOwnership.currentNumTenants > 0,
                jointLandlordEmails,
            ),
        )

        if (!jointLandlordEmails.isNullOrEmpty()) {
            jointLandlordInvitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership)
        }
    }
}
