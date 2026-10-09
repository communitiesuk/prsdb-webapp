package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_PHASE_TWO
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.RenewalDateHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import java.time.LocalDate
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import java.util.Locale

@PrsdbWebService
class PropertyRegistrationService(
    private val addressService: AddressService,
    private val licenseService: LicenseService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val userToLandlordService: UserToLandlordService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val confirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val confirmationService: PropertyRegistrationConfirmationService,
    private val jointLandlordInvitationService: JointLandlordInvitationService,
    private val propertyComplianceService: PropertyComplianceService,
    private val lettingAgentAccessService: LettingAgentAccessService,
    private val delegateToLettingAgentEmailService: DelegateToLettingAgentEmailService,
    private val featureFlagManager: FeatureFlagManager,
) {
    @Transactional
    fun registerProperty(registrationData: PropertyRegistrationDataModel): PropertyOwnership =
        with(registrationData) {
            val landlord = userToLandlordService.getCurrentLandlordForUser()
            val anniversary = landlord.anniversary ?: MonthDay.now(DateTimeHelper.UK_ZONE)

            val propertyOwnership =
                createPropertyOwnershipAndRelatedEntities(
                    registrationData,
                    landlord,
                    anniversary,
                    renewalDate ?: RenewalDateHelper.getRenewalDate(anniversary),
                )

            landlord.setAnniversaryIfAbsent(anniversary)

            if (lettingAgentEmail != null) {
                val invitation = lettingAgentAccessService.createInvitation(propertyOwnership, lettingAgentEmail)
                val deadlineDate =
                    LocalDate.now().plusDays(PROVIDE_LATER_DEADLINE_DAYS.toLong()).format(
                        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK),
                    )
                delegateToLettingAgentEmailService.sendDelegationEmailToLettingAgent(
                    propertyOwnership,
                    landlord.name,
                    lettingAgentEmail,
                    deadlineDate,
                    invitationToken = invitation.token,
                )
            }

            propertyComplianceService.saveRegistrationComplianceData(
                propertyOwnership.registrationNumber.number,
                hasGasSupply,
                gasSafetyCertIssueDate,
                gasSafetyFileUploadIds,
                gasSafetyCertProvideLater = gasSafetyCertProvideLater,
                electricalSafetyFileUploadIds,
                electricalSafetyExpiryDate,
                electricalCertType,
                electricalSafetyCertProvideLater = electricalSafetyCertProvideLater,
                epcCertificateUrl,
                epcExpiryDate,
                epcEnergyRating,
                tenancyStartedBeforeEpcExpiry,
                epcExemptionReason,
                epcMeesExemptionReason,
                epcProvideLater = epcProvideLater,
            )

            confirmationService.setLastPrnRegisteredThisSession(propertyOwnership.registrationNumber.number)

            sendConfirmationEmails(
                landlord,
                propertyOwnership,
                jointLandlordEmails,
                isDelegatedToLettingAgent,
                licenseProvideLater = licenseProvideLater,
                gasSafetyCertProvideLater = gasSafetyCertProvideLater,
                electricalSafetyCertProvideLater = electricalSafetyCertProvideLater,
                epcProvideLater = epcProvideLater,
                tenancyProvideLater = tenancyProvideLater,
            )

            propertyOwnership
        }

    private fun createPropertyOwnershipAndRelatedEntities(
        registrationData: PropertyRegistrationDataModel,
        registeringLandlord: Landlord,
        anniversary: MonthDay,
        renewalDate: LocalDate,
    ): PropertyOwnership =
        with(registrationData) {
            if (addressModel.uprn != null && propertyOwnershipRepository.existsByIsActiveTrueAndAddress_Uprn(addressModel.uprn)) {
                throw EntityExistsException("Address already registered")
            }

            val address = addressService.findOrCreateAddress(addressModel)

            val license =
                if (LicenseService.licenceShouldBeStored(licenseType)) {
                    licenseService.createLicense(licenseType, licenceNumber)
                } else {
                    null
                }

            propertyOwnershipService.createPropertyOwnership(
                ownershipType = ownershipType,
                isOccupied = isOccupied,
                numberOfHouseholds = numberOfHouseholds,
                numberOfPeople = numberOfPeople,
                numBedrooms = numBedrooms,
                billsIncludedList = billsIncludedList,
                customBillsIncluded = customBillsIncluded,
                furnishedStatus = furnishedStatus,
                rentFrequency = rentFrequency,
                customRentFrequency = customRentFrequency,
                rentAmount = rentAmount,
                registeringLandlord = registeringLandlord,
                anniversary = anniversary,
                renewalDate = renewalDate,
                propertyBuildType = propertyType,
                customPropertyType = customPropertyType,
                markedJointLandlord = markedJointLandlord,
                tenancyProvideLater = tenancyProvideLater,
                address = address,
                license = license,
                licenseProvideLater = licenseProvideLater,
                correspondenceEmail = correspondenceEmail,
                correspondenceAddressModel = correspondenceAddressModel,
            )
        }

    private fun sendConfirmationEmails(
        landlord: Landlord,
        propertyOwnership: PropertyOwnership,
        jointLandlordEmails: List<String>?,
        isDelegatedToLettingAgent: Boolean,
        licenseProvideLater: Boolean,
        gasSafetyCertProvideLater: Boolean,
        electricalSafetyCertProvideLater: Boolean,
        epcProvideLater: Boolean,
        tenancyProvideLater: Boolean,
    ) {
        // TODO: PDJB-1274: Update emails to account for org landlord (check which org email address to use, currently registrant)
        confirmationEmailSender.sendEmail(
            landlord.email,
            PropertyRegistrationConfirmationEmail(
                RegistrationNumberDataModel
                    .fromRegistrationNumber(propertyOwnership.registrationNumber)
                    .toString(),
                propertyOwnership.address.toMultiLineAddress(),
                absoluteUrlProvider.buildLandlordDashboardUri().toString(),
                propertyOwnership.isOccupied,
                jointLandlordEmails,
                isDelegatedToLettingAgent,
                isPdjb939PhaseTwoEnabled = featureFlagManager.checkFeature(PROPERTY_REGISTRATION_PHASE_TWO),
                licenseProvideLater = licenseProvideLater,
                gasSafetyCertProvideLater = gasSafetyCertProvideLater,
                electricalSafetyCertProvideLater = electricalSafetyCertProvideLater,
                epcProvideLater = epcProvideLater,
                tenancyProvideLater = tenancyProvideLater,
            ),
        )

        if (!jointLandlordEmails.isNullOrEmpty()) {
            jointLandlordInvitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, landlord)
        }
    }
}
