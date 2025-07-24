package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.constants.ONE_LOGIN_INFO_URL
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import java.net.URI

data class LocalAuthorityInvitationEmail(
    var localAuthority: LocalAuthority,
    var invitationUri: URI,
    var prsdUrl: String,
    var oneLoginUrl: String = ONE_LOGIN_INFO_URL,
) : EmailTemplateModel {
    private val localAuthorityKey = "name of council"
    private val invitationKey = "invitation url"
    private val prsdUrlKey = "prsd url"
    private val oneLoginUrlKey = "one login url"

    override val templateId = EmailTemplateId.LOCAL_AUTHORITY_INVITATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            localAuthorityKey to localAuthority.name,
            invitationKey to invitationUri.toString(),
            prsdUrlKey to prsdUrl,
            oneLoginUrlKey to oneLoginUrl,
        )
}
