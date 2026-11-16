package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.JsonDeserializationKeys
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplate
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.LetterTemplate
import kotlin.jvm.javaClass

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

    private val emailNotifyIdMap: Map<EmailTemplate, NotifyIdData> =
        loadNotifyIdData("/emails/emailTemplates.json", "Email").associateBy { EmailTemplate.valueOf(it.enumName) }

    private val letterNotifyIdMap: Map<LetterTemplate, NotifyIdData> =
        loadNotifyIdData("/letters/letterTemplates.json", "Letter").associateBy { LetterTemplate.valueOf(it.enumName) }

    private fun loadNotifyIdData(
        templateJsonPath: String,
        templateType: String,
    ): List<NotifyIdData> =
        json.decodeFromString<List<NotifyIdData>>(
            javaClass
                .getResource(templateJsonPath)
                ?.readText()
                ?.replace("\"$notifyIdKeyForEnvironment\"", "\"notifyId\"")
                ?: throw IllegalStateException("$templateType template JSON not found"),
        )

    @Serializable
    private data class NotifyIdData(
        val notifyId: String,
        val enumName: String,
    )

    fun getNotifyIdValue(emailTemplate: EmailTemplate): String = emailNotifyIdMap[emailTemplate]!!.notifyId

    fun getNotifyIdValue(letterTemplate: LetterTemplate): String = letterNotifyIdMap[letterTemplate]!!.notifyId
}
