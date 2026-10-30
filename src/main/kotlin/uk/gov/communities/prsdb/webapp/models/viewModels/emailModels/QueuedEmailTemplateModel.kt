package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

class QueuedEmailTemplateModel(
    override val template: EmailTemplate,
    private val personalisation: Map<String, String>,
) : EmailTemplateModel {
    override fun toHashMap(): HashMap<String, String> = HashMap(personalisation)
}
