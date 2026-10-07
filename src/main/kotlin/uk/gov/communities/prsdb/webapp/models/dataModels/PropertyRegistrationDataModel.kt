package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import java.math.BigDecimal
import java.time.LocalDate

data class PropertyRegistrationDataModel(
    val addressModel: AddressDataModel,
    val propertyType: PropertyType,
    val licenseType: LicensingType,
    val licenceNumber: String,
    val ownershipType: OwnershipType,
    val isOccupied: Boolean,
    val numberOfHouseholds: Int,
    val numberOfPeople: Int,
    val numBedrooms: Int?,
    val billsIncludedList: String?,
    val customBillsIncluded: String?,
    val furnishedStatus: FurnishedStatus?,
    val rentFrequency: RentFrequency?,
    val customRentFrequency: String?,
    val rentAmount: BigDecimal?,
    val customPropertyType: String?,
    val jointLandlordEmails: List<String>? = null,
    val lettingAgentEmail: String? = null,
    val markedJointLandlord: Boolean = false,
    val hasGasSupply: Boolean? = null,
    val gasSafetyCertIssueDate: LocalDate? = null,
    val gasSafetyFileUploadIds: List<Long> = emptyList(),
    val gasSafetyCertProvideLater: Boolean? = null,
    val electricalSafetyFileUploadIds: List<Long> = emptyList(),
    val electricalSafetyExpiryDate: LocalDate? = null,
    val electricalCertType: CertificateType? = null,
    val electricalSafetyCertProvideLater: Boolean? = null,
    val epcCertificateUrl: String? = null,
    val epcExpiryDate: LocalDate? = null,
    val epcEnergyRating: String? = null,
    val tenancyStartedBeforeEpcExpiry: Boolean? = null,
    val epcExemptionReason: EpcExemptionReason? = null,
    val epcMeesExemptionReason: MeesExemptionReason? = null,
    val epcProvideLater: Boolean? = null,
    val licenseProvideLater: Boolean = false,
    val tenancyProvideLater: Boolean? = null,
    val isDelegatedToLettingAgent: Boolean = false,
    val correspondenceEmail: String? = null,
    val correspondenceAddressModel: AddressDataModel? = null,
)
