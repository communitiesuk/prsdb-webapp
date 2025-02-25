package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority

data class LocalAuthorityInvitationCancellationEmail(
    val localAuthority: LocalAuthority,
) : EmailTemplateModel {
    private val localAuthorityKey = "name of council"

    override val templateId = EmailTemplateId.LOCAL_AUTHORITY_INVITATION_CANCELLATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            localAuthorityKey to localAuthority.name,
        )
}
