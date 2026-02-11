package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class LocalCouncilUserDeletionEmail(
    val councilName: String,
) : EmailTemplateModel {
    private val councilNameKey = "Council Name"
    override val template = EmailTemplate.LOCAL_COUNCIL_USER_DELETION_EMAIL

    override fun toHashMap() =
        hashMapOf(
            councilNameKey to councilName,
        )
}
