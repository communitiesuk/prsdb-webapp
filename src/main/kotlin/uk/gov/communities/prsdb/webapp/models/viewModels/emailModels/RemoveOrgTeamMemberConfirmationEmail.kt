package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.net.URI

data class RemoveOrgTeamMemberConfirmationEmail(
    val adminName: String,
    val memberName: String,
    val organisationName: String,
    val teamMembersUrl: URI,
) : EmailTemplateModel {
    private val adminNameKey = "administrators name"
    private val memberNameKey = "member name"
    private val organisationNameKey = "organisation name"
    private val teamMembersUrlKey = "team members url"

    override val template = EmailTemplate.REMOVE_ORG_TEAM_MEMBER_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            adminNameKey to adminName,
            memberNameKey to memberName,
            organisationNameKey to organisationName,
            teamMembersUrlKey to teamMembersUrl.toASCIIString(),
        )
}
