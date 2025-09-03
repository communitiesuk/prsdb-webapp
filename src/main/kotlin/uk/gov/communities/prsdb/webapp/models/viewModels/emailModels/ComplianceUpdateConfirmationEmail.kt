package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.constants.EPC_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.MEES_EXEMPTION_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PRS_EXEMPTION_URL
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class ComplianceUpdateConfirmationEmail(
    private val propertyAddress: String,
    private val registrationNumber: RegistrationNumberDataModel,
    private val dashboardUrl: String,
    complianceUpdateType: UpdateType,
) : EmailTemplateModel {
    override val template: EmailTemplate =
        when (complianceUpdateType) {
            UpdateType.VALID_GAS_SAFETY_INFORMATION -> EmailTemplate.UPDATE_GAS_SAFETY_INFORMATION_CONFIRMATION_EMAIL
            UpdateType.EXPIRED_GAS_SAFETY_INFORMATION -> EmailTemplate.UPDATE_GAS_SAFETY_EXPIRED_CONFIRMATION_EMAIL
            UpdateType.VALID_ELECTRICAL_INFORMATION -> EmailTemplate.UPDATE_ELECTRICAL_INFORMATION_CONFIRMATION_EMAIL
            UpdateType.EXPIRED_ELECTRICAL_INFORMATION -> EmailTemplate.UPDATE_ELECTRICAL_INFORMATION_EXPIRED_CONFIRMATION_EMAIL
            UpdateType.VALID_EPC_INFORMATION -> EmailTemplate.UPDATE_EPC_CONFIRMATION_EMAIL
            UpdateType.LOW_RATED_EPC_INFORMATION -> EmailTemplate.UPDATE_EPC_LOW_RATING_CONFIRMATION_EMAIL
            UpdateType.EXPIRED_EPC_INFORMATION -> EmailTemplate.UPDATE_EPC_EXPIRED_CONFIRMATION_EMAIL
            UpdateType.REMOVED_MEES_EPC_INFORMATION -> EmailTemplate.UPDATE_EPC_REMOVED_MEES_CONFIRMATION_EMAIL
            UpdateType.NO_EPC_INFORMATION -> EmailTemplate.UPDATE_EPC_NO_EPC_CONFIRMATION_EMAIL
        }

    override fun toHashMap() =
        hashMapOf(
            "single line address" to propertyAddress,
            "registration number" to registrationNumber.toString(),
            "dashboard url" to dashboardUrl,
            "mees exemption url" to MEES_EXEMPTION_GUIDE_URL,
            "epc guide url" to EPC_GUIDE_URL,
            "register exemption url" to REGISTER_PRS_EXEMPTION_URL,
        )

    enum class UpdateType {
        VALID_GAS_SAFETY_INFORMATION,
        EXPIRED_GAS_SAFETY_INFORMATION,
        VALID_ELECTRICAL_INFORMATION,
        EXPIRED_ELECTRICAL_INFORMATION,
        VALID_EPC_INFORMATION,
        LOW_RATED_EPC_INFORMATION,
        EXPIRED_EPC_INFORMATION,
        REMOVED_MEES_EPC_INFORMATION,
        NO_EPC_INFORMATION,
    }
}
