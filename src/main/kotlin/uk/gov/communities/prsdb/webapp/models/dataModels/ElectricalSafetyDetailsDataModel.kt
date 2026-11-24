package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import java.time.LocalDate

data class ElectricalSafetyDetailsDataModel(
    val certType: CertificateType?,
    val eicrExpiryDate: LocalDate?,
    val eicIssueDate: LocalDate?,
    // TODO PDJB-1679: Remove this when the EIC_ISSUE_DATE flag is removed
    val eicExpiryDate: LocalDate?,
    val uploadIds: List<Long>,
)
