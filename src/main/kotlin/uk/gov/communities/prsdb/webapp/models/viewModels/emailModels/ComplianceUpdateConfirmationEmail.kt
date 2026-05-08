package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.constants.EPC_GUIDE_URL
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import java.net.URI

data class ComplianceUpdateConfirmationEmail(
    private val propertyAddress: String,
    private val registrationNumber: RegistrationNumberDataModel,
    private val dashboardUrl: URI,
    private val complianceUpdateType: UpdateType,
    private val certificateType: String,
    private val certificateTypeLabel: String,
    private val expiryDate: String? = null,
    private val deadlineDate: String? = null,
) : EmailTemplateModel {
    override val template: EmailTemplate =
        when (complianceUpdateType) {
            UpdateType.CERTIFICATE_ADDED -> EmailTemplate.CERTIFICATE_ADDED_CONFIRMATION_EMAIL
            UpdateType.EXPIRED_CERTIFICATE_OCCUPIED -> EmailTemplate.EXPIRED_CERTIFICATE_OCCUPIED_CONFIRMATION_EMAIL
            UpdateType.EXPIRED_CERTIFICATE_UNOCCUPIED -> EmailTemplate.EXPIRED_CERTIFICATE_UNOCCUPIED_CONFIRMATION_EMAIL
            UpdateType.EXPIRED_EPC_OCCUPIED -> EmailTemplate.EXPIRED_EPC_OCCUPIED_CONFIRMATION_EMAIL
        }

    override fun toHashMap() =
        hashMapOf(
            "single line address" to propertyAddress,
            "registration number" to registrationNumber.toString(),
            "dashboard url" to dashboardUrl.toString(),
            "epc guide url" to EPC_GUIDE_URL,
            "certificate type" to certificateType,
            "certificate type label" to certificateTypeLabel,
            "expiry date" to (expiryDate ?: ""),
            "28 day deadline" to (deadlineDate ?: ""),
        )

    enum class UpdateType {
        CERTIFICATE_ADDED,
        EXPIRED_CERTIFICATE_OCCUPIED,
        EXPIRED_CERTIFICATE_UNOCCUPIED,
        EXPIRED_EPC_OCCUPIED,
    }
}
