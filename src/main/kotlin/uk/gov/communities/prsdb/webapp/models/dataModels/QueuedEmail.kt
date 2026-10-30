package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplate
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.QueuedEmailTemplateModel

@Serializable
data class QueuedEmail(
    val template: EmailTemplate,
    val recipientAddress: String,
    val personalisation: Map<String, String>,
) {
    fun toEmailTemplateModel(): EmailTemplateModel = QueuedEmailTemplateModel(template, personalisation)

    companion object {
        fun from(
            recipientAddress: String,
            email: EmailTemplateModel,
        ) = QueuedEmail(email.template, recipientAddress, email.toHashMap())
    }
}
