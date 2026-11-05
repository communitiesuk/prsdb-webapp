package uk.gov.communities.prsdb.webapp.services

import io.awspring.cloud.sqs.annotation.SqsListener
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.config.EmailQueueListenerConfig.Companion.EMAIL_QUEUE_LISTENER_CONTAINER_FACTORY
import uk.gov.communities.prsdb.webapp.config.EmailQueueListenerConfig.Companion.EMAIL_QUEUE_LISTENER_PROFILES
import uk.gov.communities.prsdb.webapp.exceptions.NotifyAllowlistException
import uk.gov.communities.prsdb.webapp.models.dataModels.QueuedEmail

@Service
@Profile(EMAIL_QUEUE_LISTENER_PROFILES)
class EmailQueueListener(
    private val emailDeliveryService: EmailDeliveryService,
    @Value("\${notify.allowlist-fallback-address}") private val allowlistFallbackAddress: String,
) {
    @SqsListener(queueNames = ["\${email-queue.name}"], factory = EMAIL_QUEUE_LISTENER_CONTAINER_FACTORY)
    fun receiveEmail(payload: String) {
        val queuedEmail = Json.decodeFromString<QueuedEmail>(payload)
        val email = queuedEmail.toEmailTemplateModel()
        try {
            emailDeliveryService.deliverEmail(queuedEmail.recipientAddress, email)
        } catch (exception: NotifyAllowlistException) {
            if (allowlistFallbackAddress.isBlank()) {
                println(
                    "${email.template.name} email to ${queuedEmail.recipientAddress} was rejected by the Notify " +
                        "allowlist and no fallback address is configured, so it has been dropped.",
                )
                return
            }
            println(
                "${email.template.name} email to ${queuedEmail.recipientAddress} was rejected by the Notify " +
                    "allowlist, so it is being re-sent to the fallback address.",
            )
            emailDeliveryService.deliverEmail(allowlistFallbackAddress, email)
        }
    }
}
