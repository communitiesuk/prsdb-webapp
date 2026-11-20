package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import java.time.LocalDate

data class PropertyReactivationDataModel(
    val licensing: LicensingDetailsDataModel? = null,
    val gasSafety: ReactivationGasSafetyDataModel? = null,
    val electricalSafety: ReactivationElectricalSafetyDataModel? = null,
    val epc: ReactivationEpcDataModel? = null,
    val tenancyDetails: TenancyDetailsDataModel? = null,
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
