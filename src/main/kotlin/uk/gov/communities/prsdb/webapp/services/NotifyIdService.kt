package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.ApplicationScope
import uk.gov.communities.prsdb.webapp.constants.JsonDeserializationKeys
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplate
import kotlin.jvm.javaClass

@ApplicationScope
@Service
class NotifyIdService(
    @Value("\${notify.use-production-notify}") private val useProductionNotify: Boolean,
) {
    private val notifyIdKeyForEnvironment: String =
        if (useProductionNotify) {
            JsonDeserializationKeys.PRODUCTION_NOTIFY_ID_KEY
        } else {
            JsonDeserializationKeys.TEST_NOTIFY_ID_KEY
        }

    private val json: Json = Json { ignoreUnknownKeys = true }

    private val notifyIdMap: Map<EmailTemplate, NotifyIdData> =
        json
            .decodeFromString<List<NotifyIdData>>(
                javaClass
                    .getResource("/emails/emailTemplates.json")
                    ?.readText()
                    ?.replace("\"$notifyIdKeyForEnvironment\"", "\"notifyId\"")
                    ?: throw IllegalStateException("Email template JSON not found"),
            ).associateBy { EmailTemplate.valueOf(it.enumName) }

    @Serializable
    private data class NotifyIdData(
        val notifyId: String,
        val enumName: String,
    )

    fun getNotifyIdValue(emailTemplate: EmailTemplate): String = notifyIdMap[emailTemplate]!!.notifyId
}
