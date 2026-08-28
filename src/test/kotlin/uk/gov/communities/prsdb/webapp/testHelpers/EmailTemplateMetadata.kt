package uk.gov.communities.prsdb.webapp.testHelpers

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class EmailTemplateMetadata(
    val id: String? = null,
    val enumName: String,
    val subject: String,
    val bodyLocation: String,
)

class EmailTemplateMetadataFactory(
    notifyEnvironment: NotifyEnvironment?,
) {
    private val templateIdJsonKey: String? = notifyEnvironment?.templateIdJsonKey

    val json: Json = Json { ignoreUnknownKeys = true }

    val metadataList: List<EmailTemplateMetadata> =
        json.decodeFromString<List<EmailTemplateMetadata>>(
            javaClass
                .getResource("/emails/emailTemplates.json")
                ?.readText()
                ?.replace("\"$templateIdJsonKey\"", "\"id\"")
                ?: "",
        )
}
