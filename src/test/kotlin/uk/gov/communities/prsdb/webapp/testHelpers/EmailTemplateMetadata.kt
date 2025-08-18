package uk.gov.communities.prsdb.webapp.testHelpers

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Serializable
data class EmailTemplateMetadata(
    val id: String? = null,
    val enumName: String,
    val subject: String,
    val bodyLocation: String,
)

@Service
class EmailTemplateMetadataFactory(
    @Value("\${notify.use-production-notify}")
    useProductionNotify: Boolean?,
) {
    private val testIdName: String? = useProductionNotify?.let { if (it) "prod_id" else "test_id" }

    val json: Json = Json { ignoreUnknownKeys = true }

    val metadataList: List<EmailTemplateMetadata> =
        json.decodeFromString<List<EmailTemplateMetadata>>(
            javaClass
                .getResource("/emails/emailTemplates.json")
                ?.readText()
                ?.replace("\"$testIdName\"", "\"id\"")
                ?: "",
        )
}
