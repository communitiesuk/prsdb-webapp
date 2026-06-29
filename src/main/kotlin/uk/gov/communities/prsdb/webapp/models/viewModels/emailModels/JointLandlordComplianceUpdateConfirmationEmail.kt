package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import java.net.URI

data class JointLandlordComplianceUpdateConfirmationEmail(
    private val landlordName: String,
    private val multiLineAddress: String,
    private val registrationNumber: RegistrationNumberDataModel,
    private val dashboardUrl: URI,
    private val certificateType: String,
    private val certificateTypeLabel: String,
    private val expiryDate: String,
) : EmailTemplateModel {
    override val template: EmailTemplate = EmailTemplate.COMPLIANCE_UPDATED_CONFIRMATION_EMAIL

    override fun toHashMap() =
        hashMapOf(
            "landlord name" to landlordName,
            "multi line address" to multiLineAddress,
            "registration number" to registrationNumber.toString(),
            "dashboard url" to dashboardUrl.toString(),
            "certificate type" to certificateType,
            "certificate type label" to certificateTypeLabel,
            "expiry date" to expiryDate,
        )
}
