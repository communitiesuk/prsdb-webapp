package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import java.net.URI

data class LocalAuthorityInvitationEmail(
    var localAuthority: LocalAuthority,
    var invitationUri: URI,
) : EmailTemplateModel {
    private val localAuthorityKey = "name of council"
    private val invitationKey = "invitation url"

    override val templateId = EmailTemplateId.LOCAL_AUTHORITY_INVITATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            localAuthorityKey to localAuthority.name,
            invitationKey to invitationUri.toString(),
        )
}
