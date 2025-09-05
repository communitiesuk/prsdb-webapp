package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class LocalAuthorityUserDeletionAdminEmail(
    val councilName: String,
    val email: String,
    val userName: String,
    val prsdURL: String,
) : EmailTemplateModel {
    private val councilNameKey = "Local Council Name"
    private val emailKey = "User Email"
    private val userNameKey = "User Name"
    private val prsdURLKey = "Base URL"
    override val template = EmailTemplate.LOCAL_AUTHORITY_USER_DELETION_INFORM_ADMIN_EMAIL

    override fun toHashMap() =
        hashMapOf(
            councilNameKey to councilName,
            emailKey to email,
            userNameKey to userName,
            prsdURLKey to prsdURL,
        )
}
