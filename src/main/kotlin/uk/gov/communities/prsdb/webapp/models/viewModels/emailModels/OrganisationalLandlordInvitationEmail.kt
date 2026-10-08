package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.net.URI

data class OrganisationalLandlordInvitationEmail(
    val organisationName: String,
    val invitationUri: URI,
) : EmailTemplateModel {
    private val organisationNameKey = "organisation name"
    private val invitationUrlKey = "invitation url"

    override val template = EmailTemplate.ORGANISATIONAL_LANDLORD_INVITATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            organisationNameKey to organisationName,
            invitationUrlKey to invitationUri.toString(),
        )
}
