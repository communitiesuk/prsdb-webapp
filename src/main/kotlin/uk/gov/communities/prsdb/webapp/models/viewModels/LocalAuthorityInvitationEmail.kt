package uk.gov.communities.prsdb.webapp.models.viewModels

data class LocalAuthorityInvitationEmail(
    var localAuthorityName: String,
    var invitationUrl: String,
) : EmailTemplateModel {
    private val localAuthorityKey = "name of council"
    private val invitationKey = "invite url"

    override val templateId = EmailTemplateId.LOCAL_AUTHORITY_INVITATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            localAuthorityKey to localAuthorityName,
            invitationKey to invitationUrl,
        )
}
