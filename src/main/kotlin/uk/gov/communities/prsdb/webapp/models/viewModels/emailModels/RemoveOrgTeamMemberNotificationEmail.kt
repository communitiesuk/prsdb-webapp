package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class RemoveOrgTeamMemberNotificationEmail(
    val memberName: String,
    val organisationName: String,
) : EmailTemplateModel {
    private val memberNameKey = "member name"
    private val organisationNameKey = "organisation name"

    override val template = EmailTemplate.REMOVE_ORG_TEAM_MEMBER_NOTIFICATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            memberNameKey to memberName,
            organisationNameKey to organisationName,
        )
}
