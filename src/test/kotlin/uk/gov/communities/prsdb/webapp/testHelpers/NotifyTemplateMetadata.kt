package uk.gov.communities.prsdb.webapp.testHelpers

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class NotifyTemplateMetadata(
    val id: String? = null,
    val enumName: String,
    val subject: String,
    val bodyLocation: String,
)

class NotifyTemplateMetadataFactory(
    notifyEnvironment: NotifyEnvironment?,
    templateJsonPath: String = "/emails/emailTemplates.json",
) {
    private val templateIdJsonKey: String? = notifyEnvironment?.templateIdJsonKey

    val json: Json = Json { ignoreUnknownKeys = true }

    val metadataList: List<NotifyTemplateMetadata> =
        json.decodeFromString<List<NotifyTemplateMetadata>>(
            javaClass
                .getResource(templateJsonPath)
                ?.readText()
                ?.replace("\"$templateIdJsonKey\"", "\"id\"")
                ?: "",
        )
}
