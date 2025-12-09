package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import java.net.URI

data class LocalCouncilAdminInvitationEmail(
    var localCouncil: LocalCouncil,
    var invitationUri: URI,
) : EmailTemplateModel {
    private val localCouncilKey = "name of council"
    private val invitationKey = "invitation url"

    override val template = EmailTemplate.LOCAL_COUNCIL_ADMIN_INVITATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            localCouncilKey to localCouncil.name,
            invitationKey to invitationUri.toString(),
        )
}
