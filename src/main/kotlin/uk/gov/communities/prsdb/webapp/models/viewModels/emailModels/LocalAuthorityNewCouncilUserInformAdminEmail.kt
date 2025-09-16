package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

class LocalAuthorityNewCouncilUserInformAdminEmail(
    val councilName: String,
    val email: String,
    val prsdURL: String,
) : EmailTemplateModel {
    private val councilNameKey = "Local Council Name"
    private val emailKey = "User Email"
    private val prsdURLKey = "Base URL"
    override val template = EmailTemplate.LOCAL_AUTHORITY_NEW_COUNCIL_USER_INFORM_ADMIN_EMAIL

    override fun toHashMap() =
        hashMapOf(
            councilNameKey to councilName,
            emailKey to email,
            prsdURLKey to prsdURL,
        )
}
