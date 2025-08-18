package uk.gov.communities.prsdb.webapp.testHelpers

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class EmailTemplateMetadata(
    val id: String,
    val enumName: String,
    val subject: String,
    val bodyLocation: String,
) {
    companion object {
        val json: Json = Json { ignoreUnknownKeys = true }

        // Has to be static so it can be used in parameterized tests
        @JvmStatic
        val metadataList: List<EmailTemplateMetadata> =
            json.decodeFromString<List<EmailTemplateMetadata>>(
                javaClass
                    .getResource("/emails/emailTemplates.json")
                    ?.readText()
                    ?.replace("\"test_id\"", "\"id\"")
                    ?: "",
            )
    }
}
