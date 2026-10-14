package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.net.URI

data class OrganisationalLandlordInvitationAcceptedAdminEmail(
    val recipientName: String,
    val organisationName: String,
    val dashboardUrl: URI,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val organisationNameKey = "organisation name"
    private val dashboardUrlKey = "dashboard url"

    override val template = EmailTemplate.ORGANISATIONAL_LANDLORD_INVITATION_ACCEPTED_ADMIN_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            organisationNameKey to organisationName,
            dashboardUrlKey to dashboardUrl.toASCIIString(),
        )
}
