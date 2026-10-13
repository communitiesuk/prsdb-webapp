package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.net.URI

data class ChangeOrgTeamMemberAccessLevelEmail(
    val memberName: String,
    val roleName: String,
    val organisationName: String,
    val isAdmin: Boolean,
    val dashboardUrl: URI,
) : EmailTemplateModel {
    private val memberNameKey = "name"
    private val roleNameKey = "role name"
    private val organisationNameKey = "organisation name"
    private val isAdminKey = "isAdmin"
    private val dashboardUrlKey = "dashboard url"

    override val template = EmailTemplate.CHANGE_ORG_TEAM_MEMBER_ACCESS_LEVEL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            memberNameKey to memberName,
            roleNameKey to roleName,
            organisationNameKey to organisationName,
            isAdminKey to isAdmin.toNotifyYesNo(),
            dashboardUrlKey to dashboardUrl.toASCIIString(),
        )
}
