package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplate
import kotlin.test.assertEquals

class NotifyIdServiceTests {
    @Serializable
    private data class EmailTemplateMetadata(
        val test_id: String? = null,
        val prod_id: String? = null,
        val enumName: String,
        val subject: String,
        val bodyLocation: String,
    )

    companion object {
        private val metadataList =
            Json.decodeFromString<List<EmailTemplateMetadata>>(
                javaClass.getResource("/emails/emailTemplates.json")!!.readText(),
            )
    }

    @Disabled("Disabled until we can include production IDs")
    @ParameterizedTest
    @EnumSource(EmailTemplate::class)
    fun `getIdValue returns production Notify id values if production is set`(id: EmailTemplate) {
        // Arrange
        val service = NotifyIdService(useProductionNotify = true)
        val metadata = metadataList.firstOrNull { it.enumName == id.name }

        // Act
        val idValue = service.getNotifyIdValue(id)

        // Assert
        assertEquals(metadata?.prod_id, idValue)
    }

    @ParameterizedTest
    @EnumSource(EmailTemplate::class)
    fun `getIdValue returns test Notify id values if production is not set`(id: EmailTemplate) {
        // Arrange
        val service = NotifyIdService(useProductionNotify = false)
        val metadata = metadataList.firstOrNull { it.enumName == id.name }

        // Act
        val idValue = service.getNotifyIdValue(id)

        // Assert
        assertEquals(metadata?.test_id, idValue)
    }
}
