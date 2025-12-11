package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class IncompletePropertyReminderEmail(
    val singleLineAddress: String,
    val prsdUrl: String,
) : EmailTemplateModel {
    private val addressKey = "property address"
    private val prsdUrlKey = "prsd url"

    override val template = EmailTemplate.INCOMPLETE_PROPERTY_REMINDER_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            addressKey to singleLineAddress,
            prsdUrlKey to prsdUrl,
        )
}
