package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.net.URI

data class LandlordUpdateConfirmation(
    val registrationNumber: String,
    val dashboardUrl: URI,
    val updatedDetail: String,
) : EmailTemplateModel {
    private val registrationNumberKey = "registration number"
    private val dashboardUrlKey = "dashboard url"
    private val updatedDetailKey = "updated detail"

    override val templateId = EmailTemplateId.LANDLORD_UPDATE_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            registrationNumberKey to registrationNumber,
            dashboardUrlKey to dashboardUrl.toASCIIString(),
            updatedDetailKey to updatedDetail,
        )
}
