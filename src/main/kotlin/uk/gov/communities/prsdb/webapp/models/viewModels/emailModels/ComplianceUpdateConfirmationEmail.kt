package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import java.net.URI

data class ComplianceUpdateConfirmationEmail(
    private val landlordName: String,
    private val multiLineAddress: String,
    private val registrationNumber: RegistrationNumberDataModel,
    private val dashboardUrl: URI,
    private val newCertificateUrl: URI,
    private val complianceUpdateType: UpdateType,
    private val certificateType: String,
    private val certificateTypeLabel: String,
    private val expiryDate: String? = null,
    private val deadlineDate: String? = null,
    private val isJointLandlord: Boolean = false,
) : EmailTemplateModel {
    override val template: EmailTemplate =
        when (complianceUpdateType) {
            UpdateType.CERTIFICATE_ADDED ->
                if (isJointLandlord) {
                    EmailTemplate.JOINT_LANDLORD_COMPLIANCE_UPDATED_CONFIRMATION_EMAIL
                } else {
                    EmailTemplate.COMPLIANCE_UPDATED_CONFIRMATION_EMAIL
                }
            UpdateType.EXPIRED_CERTIFICATE_OCCUPIED -> EmailTemplate.COMPLIANCE_EXPIRED_OCCUPIED_CONFIRMATION_EMAIL
            UpdateType.EXPIRED_CERTIFICATE_UNOCCUPIED -> EmailTemplate.COMPLIANCE_EXPIRED_UNOCCUPIED_CONFIRMATION_EMAIL
            UpdateType.EXPIRED_EPC_OCCUPIED -> EmailTemplate.COMPLIANCE_EXPIRED_OCCUPIED_EPC_CONFIRMATION_EMAIL
        }

    override fun toHashMap() =
        hashMapOf(
            "landlord name" to landlordName,
            "multi line address" to multiLineAddress,
            "registration number" to registrationNumber.toString(),
            "dashboard url" to dashboardUrl.toString(),
            "new certificate url" to newCertificateUrl.toString(),
            "new epc url" to GET_NEW_EPC_URL,
            "certificate type" to certificateType,
            "certificate type label" to certificateTypeLabel,
            // These default to an empty string as not all compliance templates use them, and some EPC's may not have an expiry date
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
