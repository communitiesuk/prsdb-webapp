package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.constants.ONE_LOGIN_INFO_URL
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import java.net.URI

data class LocalCouncilInvitationEmail(
    var localCouncil: LocalCouncil,
    var invitationUri: URI,
    var prsdUrl: String,
    var oneLoginUrl: String = ONE_LOGIN_INFO_URL,
) : EmailTemplateModel {
    private val localAuthorityKey = "name of council"
    private val invitationKey = "invitation url"
    private val prsdUrlKey = "prsd url"
    private val oneLoginUrlKey = "one login url"

    override val template = EmailTemplate.LOCAL_COUNCIL_INVITATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            localAuthorityKey to localCouncil.name,
            invitationKey to invitationUri.toString(),
            prsdUrlKey to prsdUrl,
            oneLoginUrlKey to oneLoginUrl,
        )
}
