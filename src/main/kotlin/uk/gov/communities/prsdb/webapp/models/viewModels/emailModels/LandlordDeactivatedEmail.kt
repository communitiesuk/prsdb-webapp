package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.constants.enums.LandlordDeactivationReason

data class LandlordDeactivatedEmail(
    val recipientName: String,
    val registrationNumber: String,
    val reason: LandlordDeactivationReason,
    val dashboardUrl: String,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val registrationNumberKey = "landlord number"
    private val dashboardUrlKey = "dashboard url"

    override val template =
        when (reason) {
            LandlordDeactivationReason.NO_PROPERTIES_REGISTERED -> EmailTemplate.LANDLORD_DEACTIVATED_NO_PROPERTIES_REGISTERED
        }

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            registrationNumberKey to registrationNumber,
            dashboardUrlKey to dashboardUrl,
        )
}
