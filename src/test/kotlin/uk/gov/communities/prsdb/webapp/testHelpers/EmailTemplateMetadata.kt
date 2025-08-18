package uk.gov.communities.prsdb.webapp.testHelpers

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.JsonDeserializationKeys

@Serializable
data class EmailTemplateMetadata(
    val id: String? = null,
    val enumName: String,
    val subject: String,
    val bodyLocation: String,
)

@PrsdbWebService
class EmailTemplateMetadataFactory(
    @Value("\${notify.use-production-notify}")
    useProductionNotify: Boolean?,
) {
    private val testIdName: String? =
        useProductionNotify?.let {
            if (it) {
                JsonDeserializationKeys.PRODUCTION_NOTIFY_ID_KEY
            } else {
                JsonDeserializationKeys.TEST_NOTIFY_ID_KEY
            }
        }

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
