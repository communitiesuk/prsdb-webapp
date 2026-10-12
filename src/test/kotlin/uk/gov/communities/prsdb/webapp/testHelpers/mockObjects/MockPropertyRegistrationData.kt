package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationDataModel
import java.math.BigDecimal
import java.time.LocalDate

class MockPropertyRegistrationData {
    companion object {
        fun createPropertyRegistrationDataModel(
            addressModel: AddressDataModel = AddressDataModel("1 Example Road, EG1 2AB"),
            propertyType: PropertyType = PropertyType.DETACHED_HOUSE,
            licenseType: LicensingType = LicensingType.NO_LICENSING,
            licenceNumber: String = "",
            ownershipType: OwnershipType = OwnershipType.FREEHOLD,
            isOccupied: Boolean = false,
            numberOfHouseholds: Int = 0,
            numberOfPeople: Int = 0,
            numBedrooms: Int? = null,
            billsIncludedList: String? = null,
            customBillsIncluded: String? = null,
            furnishedStatus: FurnishedStatus? = null,
            rentFrequency: RentFrequency? = null,
            customRentFrequency: String? = null,
            rentAmount: BigDecimal? = null,
            customPropertyType: String? = null,
            jointLandlordEmails: List<String>? = null,
            lettingAgentEmail: String? = null,
            markedJointLandlord: Boolean = false,
            hasGasSupply: Boolean? = null,
            gasSafetyCertIssueDate: LocalDate? = null,
            gasSafetyFileUploadIds: List<Long> = emptyList(),
            gasSafetyCertProvideLater: Boolean = false,
            electricalSafetyFileUploadIds: List<Long> = emptyList(),
            electricalSafetyExpiryDate: LocalDate? = null,
            electricalCertType: CertificateType? = null,
            electricalSafetyCertProvideLater: Boolean = false,
            epcCertificateUrl: String? = null,
            epcExpiryDate: LocalDate? = null,
            epcEnergyRating: String? = null,
            tenancyStartedBeforeEpcExpiry: Boolean? = null,
            epcExemptionReason: EpcExemptionReason? = null,
            epcMeesExemptionReason: MeesExemptionReason? = null,
            epcProvideLater: Boolean = false,
            licenseProvideLater: Boolean = false,
            tenancyProvideLater: Boolean = false,
            isDelegatedToLettingAgent: Boolean = false,
            correspondenceEmail: String? = null,
            correspondenceAddressModel: AddressDataModel? = null,
        ) = PropertyRegistrationDataModel(
            addressModel = addressModel,
            propertyType = propertyType,
            licenseType = licenseType,
            licenceNumber = licenceNumber,
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
            customPropertyType = customPropertyType,
            jointLandlordEmails = jointLandlordEmails,
            lettingAgentEmail = lettingAgentEmail,
            markedJointLandlord = markedJointLandlord,
            hasGasSupply = hasGasSupply,
            gasSafetyCertIssueDate = gasSafetyCertIssueDate,
            gasSafetyFileUploadIds = gasSafetyFileUploadIds,
            gasSafetyCertProvideLater = gasSafetyCertProvideLater,
            electricalSafetyFileUploadIds = electricalSafetyFileUploadIds,
            electricalSafetyExpiryDate = electricalSafetyExpiryDate,
            electricalCertType = electricalCertType,
            electricalSafetyCertProvideLater = electricalSafetyCertProvideLater,
            epcCertificateUrl = epcCertificateUrl,
            epcExpiryDate = epcExpiryDate,
            epcEnergyRating = epcEnergyRating,
            tenancyStartedBeforeEpcExpiry = tenancyStartedBeforeEpcExpiry,
            epcExemptionReason = epcExemptionReason,
            epcMeesExemptionReason = epcMeesExemptionReason,
            epcProvideLater = epcProvideLater,
            licenseProvideLater = licenseProvideLater,
            tenancyProvideLater = tenancyProvideLater,
            isDelegatedToLettingAgent = isDelegatedToLettingAgent,
            correspondenceEmail = correspondenceEmail,
            correspondenceAddressModel = correspondenceAddressModel,
        )
    }
}
