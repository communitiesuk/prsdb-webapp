package uk.gov.communities.prsdb.webapp.models.dataModels.updateModels

import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import java.time.LocalDate

data class PropertyComplianceUpdateModel(
    val gasSafetyCertUpdate: GasSafetyCertUpdateModel? = null,
    val eicrUpdate: EicrUpdateModel? = null,
    val epcUpdate: EpcUpdateModel? = null,
)

data class GasSafetyCertUpdateModel(
    val s3Key: String? = null,
    val issueDate: LocalDate? = null,
    val engineerNum: String? = null,
    val exemptionReason: GasSafetyExemptionReason? = null,
    val exemptionOtherReason: String? = null,
)

data class EicrUpdateModel(
    val s3Key: String? = null,
    val issueDate: LocalDate? = null,
    val exemptionReason: EicrExemptionReason? = null,
    val exemptionOtherReason: String? = null,
)

data class EpcUpdateModel(
    val url: String? = null,
    val expiryDate: LocalDate? = null,
    val tenancyStartedBeforeExpiry: Boolean? = null,
    val energyRating: String? = null,
    val exemptionReason: EpcExemptionReason? = null,
    val meesExemptionReason: MeesExemptionReason? = null,
)
