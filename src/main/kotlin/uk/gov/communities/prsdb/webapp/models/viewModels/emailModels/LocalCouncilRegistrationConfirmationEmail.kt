package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class LocalCouncilRegistrationConfirmationEmail(
    val councilName: String,
    val prsdURL: String,
    val isAdmin: Boolean,
) : EmailTemplateModel {
    private val isAdminKey = "isAdmin"
    private val councilNameKey = "Council Name"
    private val prsdURLKey = "PRSD URL"
    override val template = EmailTemplate.LOCAL_COUNCIL_REGISTRATION_CONFIRMATION_EMAIL

    override fun toHashMap() =
        hashMapOf(
            councilNameKey to councilName,
            prsdURLKey to prsdURL,
            isAdminKey to if (isAdmin) "yes" else "no",
        )
}
