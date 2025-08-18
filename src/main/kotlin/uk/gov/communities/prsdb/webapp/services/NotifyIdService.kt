package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.ApplicationScope
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateId
import kotlin.jvm.javaClass

@ApplicationScope
@Service
class NotifyIdService(
    @Value("\${notify.use-production-notify}") private val useProductionNotify: Boolean,
) {
    private val testIdName: String = if (useProductionNotify) "prod_id" else "test_id"

    private val json: Json = Json { ignoreUnknownKeys = true }

    private val notifyIdMap: Map<EmailTemplateId, NotifyIdData> =
        json
            .decodeFromString<List<NotifyIdData>>(
                javaClass
                    .getResource("/emails/emailTemplates.json")
                    ?.readText()
                    ?.replace("\"$testIdName\"", "\"id\"") ?: throw IllegalStateException("Email template JSON not found"),
            ).associateBy { EmailTemplateId.valueOf(it.enumName) }

    @Serializable
    private data class NotifyIdData(
        val id: String,
        val enumName: String,
    )

    fun getIdValue(id: EmailTemplateId): String = notifyIdMap[id]!!.id
}
