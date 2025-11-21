package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil

data class LocalCouncilInvitationCancellationEmail(
    val localCouncil: LocalCouncil,
) : EmailTemplateModel {
    private val localCouncilKey = "name of council"

    override val template = EmailTemplate.LOCAL_COUNCIL_INVITATION_CANCELLATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            localCouncilKey to localCouncil.name,
        )
}
