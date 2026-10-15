package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.net.URI

data class OrganisationalLandlordInvitationAcceptedEmail(
    val recipientName: String,
    val roleName: String,
    val organisationName: String,
    val isAdmin: Boolean,
    val dashboardUrl: URI,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val roleNameKey = "role name"
    private val organisationNameKey = "organisation name"
    private val isAdminKey = "isAdmin"
    private val dashboardUrlKey = "dashboard url"

    override val template = EmailTemplate.ORGANISATIONAL_LANDLORD_INVITATION_ACCEPTED_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            roleNameKey to roleName,
            organisationNameKey to organisationName,
            isAdminKey to isAdmin.toNotifyYesNo(),
            dashboardUrlKey to dashboardUrl.toASCIIString(),
        )
}
