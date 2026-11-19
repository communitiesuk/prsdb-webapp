package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import java.math.BigDecimal
import java.time.LocalDate

data class PropertyReactivationDataModel(
    val licensing: ReactivationLicensingDataModel? = null,
    val gasSafety: ReactivationGasSafetyDataModel? = null,
    val electricalSafety: ReactivationElectricalSafetyDataModel? = null,
    val epc: ReactivationEpcDataModel? = null,
    val tenancyDetails: ReactivationTenancyDetailsDataModel? = null,
)

data class ReactivationLicensingDataModel(
    val licensingType: LicensingType,
    val licenceNumber: String?,
)

data class ReactivationGasSafetyDataModel(
    val hasGasSupply: Boolean,
    val issueDate: LocalDate?,
    val uploadIds: List<Long>,
)

data class ReactivationElectricalSafetyDataModel(
    val certType: CertificateType?,
    val eicrExpiryDate: LocalDate?,
    val eicIssueDate: LocalDate?,
    // TODO PDJB-1679: Remove this when the EIC_ISSUE_DATE flag is removed
    val eicExpiryDate: LocalDate?,
    val uploadIds: List<Long>,
)

data class ReactivationEpcDataModel(
    val certificateUrl: String?,
    val expiryDate: LocalDate?,
    val energyRating: String?,
    val tenancyStartedBeforeExpiry: Boolean?,
    val exemptionReason: EpcExemptionReason?,
    val meesExemptionReason: MeesExemptionReason?,
)

data class ReactivationTenancyDetailsDataModel(
    val numberOfHouseholds: Int,
    val numberOfPeople: Int,
    val billsIncludedList: String?,
    val customBillsIncluded: String?,
    val furnishedStatus: FurnishedStatus,
    val rentFrequency: RentFrequency,
    val customRentFrequency: String?,
    val rentAmount: BigDecimal,
)
