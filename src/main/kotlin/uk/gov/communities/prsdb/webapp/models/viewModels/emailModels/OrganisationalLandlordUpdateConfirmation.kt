package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.net.URI

data class OrganisationalLandlordUpdateConfirmation(
    val dashboardUrl: URI,
    val updatedDetail: String,
) : EmailTemplateModel {
    private val dashboardUrlKey = "dashboard url"
    private val updatedDetailKey = "updated detail"

    override val template = EmailTemplate.ORGANISATIONAL_LANDLORD_UPDATE_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            dashboardUrlKey to dashboardUrl.toASCIIString(),
            updatedDetailKey to updatedDetail,
        )
}
