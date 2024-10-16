package uk.gov.communities.prsdb.webapp.notify

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.communities.prsdb.webapp.viewmodel.EmailTemplateId
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.Template
import uk.gov.service.notify.TemplateList

@EnabledIf("canFetchNotifyTemplates")
class NotifyEmailTemplateTests {
    companion object NotifyTestsCompanion {
        val jsonMetadataList = javaClass.getResource("/emails/emailTemplates.json")?.readText() ?: ""
        lateinit var notifyTemplates: TemplateList

        fun haveNotifyTemplatesBeenFetched() = ::notifyTemplates.isInitialized

        fun fetchNotifyTemplates() {
            val apikey = System.getenv("EMAILNOTIFICATIONS_APIKEY")
            val notifyClient = NotificationClient(apikey)
            notifyTemplates = notifyClient.getAllTemplates("email")
        }

        @JvmStatic
        fun canFetchNotifyTemplates(): Boolean = System.getenv("EMAILNOTIFICATIONS_APIKEY") != null
    }

    @BeforeEach
    fun getNotifyTemplatesOnce() {
        if (!haveNotifyTemplatesBeenFetched()) {
            fetchNotifyTemplates()
        }
    }

    @ParameterizedTest
    @EnumSource(EmailTemplateId::class)
    fun `notify contains a template for each template id`(id: EmailTemplateId) {
        notifyTemplates.templates.single { template -> template.id.toString() == id.idValue }
    }

    @ParameterizedTest
    @EnumSource(EmailTemplateId::class)
    fun `there is a source controlled copy for each template id`(id: EmailTemplateId) {
        var metadataList = Json.decodeFromString<List<EmailTemplateMetadata>>(jsonMetadataList)

        metadataList.single { templateMetadata -> templateMetadata.id == id.idValue }
    }

    @Test
    fun `all source controlled templates match their notify equivalent`() {
        // Arrange
        var metadataList = Json.decodeFromString<List<EmailTemplateMetadata>>(jsonMetadataList)

        for (metadata in metadataList) {
            println("Testing template \"${metadata.name}\"...")
            // Act
            var templateId = metadata.id
            var notifyTemplate = notifyTemplates.templates.single { template -> template.id.toString() == templateId }

            // Assert
            assertBodiesMatch(metadata, notifyTemplate)
            Assertions.assertEquals(metadata.name, notifyTemplate.name)
            Assertions.assertEquals(metadata.subject, notifyTemplate.subject.orElse(null))

            println("Template \"${metadata.name}\" matches Notify")
        }
    }

    private fun assertBodiesMatch(
        metadata: EmailTemplateMetadata,
        notifyTemplate: Template,
    ) {
        var storedBody = javaClass.getResource(metadata.bodyLocation)?.readText() ?: ""

        // We don't care about line ending types: convert to LF before comparison
        var cleanedStoredBody = storedBody.replace("\r", "")
        var notifyBody = notifyTemplate.body

        // Notify returns body with CRLF end lines: convert to LF before comparison
        var cleanedNotifyBody = notifyBody.replace("\r", "")

        Assertions.assertEquals(cleanedStoredBody, cleanedNotifyBody)
    }

    @Serializable
    data class EmailTemplateMetadata(
        val id: String,
        val name: String,
        val subject: String,
        val bodyLocation: String,
    )
}
