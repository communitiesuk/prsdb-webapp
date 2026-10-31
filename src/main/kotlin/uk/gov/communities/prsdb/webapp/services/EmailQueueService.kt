package uk.gov.communities.prsdb.webapp.services

import io.awspring.cloud.sqs.operations.SqsTemplate
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.dataModels.QueuedEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel

@Service
class EmailQueueService(
    private val sqsTemplate: SqsTemplate,
    @Value("\${email-queue.name}") private val queueName: String,
) {
    fun enqueue(
        recipientAddress: String,
        email: EmailTemplateModel,
    ) {
        val payload = Json.encodeToString(QueuedEmail.from(recipientAddress, email))
        try {
            sqsTemplate.send(queueName, payload)
        } catch (exception: RuntimeException) {
            throw TransientEmailSentException(
                "The email could not be added to the email queue, but retrying may work.",
                exception,
            )
        }
    }
}
