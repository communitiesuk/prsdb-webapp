package uk.gov.communities.prsdb.webapp.testHelpers

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class EmailTemplateMetadata(
    val id: String,
    val name: String,
    val subject: String,
    val bodyLocation: String,
) {
    override fun toString() = name

    companion object {
        // Has to be static so it can be used in parameterized tests
        @JvmStatic
        val metadataList: List<EmailTemplateMetadata> =
            Json.decodeFromString<List<EmailTemplateMetadata>>(
                javaClass.getResource("/emails/emailTemplates.json")?.readText() ?: "",
            )
    }
}
