package uk.gov.communities.prsdb.webapp.models.dataModels.updateModels

import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import java.time.LocalDate

data class PropertyComplianceUpdateModel(
    val gasSafetyCertUpdate: GasSafetyCertUpdateModel? = null,
    val eicrUpdate: EicrUpdateModel? = null,
    val epcUpdate: EpcUpdateModel? = null,
)

data class GasSafetyCertUpdateModel(
    val fileUploadId: Long? = null,
    val issueDate: LocalDate? = null,
    val engineerNum: String? = null,
    val exemptionReason: GasSafetyExemptionReason? = null,
    val exemptionOtherReason: String? = null,
)

data class EicrUpdateModel(
    val fileUploadId: Long? = null,
    val issueDate: LocalDate? = null,
    val exemptionReason: EicrExemptionReason? = null,
    val exemptionOtherReason: String? = null,
)

data class EpcUpdateModel(
    val epcDataModel: EpcDataModel? = null,
    val url: String? = null,
    val tenancyStartedBeforeExpiry: Boolean? = null,
    val exemptionReason: EpcExemptionReason? = null,
    val meesExemptionReason: MeesExemptionReason? = null,
) {
    val energyRating: String? = epcDataModel?.energyRating
    val expiryDate: LocalDate? = epcDataModel?.expiryDate?.toJavaLocalDate()
}
