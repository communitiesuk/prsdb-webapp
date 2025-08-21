package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class BetaFeedbackEmail(
    val feedback: String,
    val email: String?,
    val referrer: String?,
) : EmailTemplateModel {
    override val template = EmailTemplate.BETA_FEEDBACK_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            "feedback" to feedback,
            "email" to (email ?: "Email not provided"),
            "referrer" to (referrer ?: "Referrer not provided"),
        )
}
