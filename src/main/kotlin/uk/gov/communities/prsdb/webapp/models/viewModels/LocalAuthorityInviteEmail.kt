package uk.gov.communities.prsdb.webapp.models.viewModels

data class LocalAuthorityInviteEmail(
    var localAuthorityName: String,
    var inviteUrl: String,
) : EmailTemplateModel {
    private val localAuthorityKey = "name of council"
    private val inviteKey = "invite url"

    override val templateId = EmailTemplateId.LOCAL_AUTHORITY_INVITE_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            localAuthorityKey to localAuthorityName,
            inviteKey to inviteUrl,
        )
}
