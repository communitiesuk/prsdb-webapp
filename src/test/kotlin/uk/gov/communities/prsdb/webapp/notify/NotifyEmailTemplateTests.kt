package uk.gov.communities.prsdb.webapp.notify

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.condition.EnabledIf
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import uk.gov.communities.prsdb.webapp.config.NotifyConfig
import uk.gov.communities.prsdb.webapp.models.emailModels.EmailTemplateId
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.Template
import uk.gov.service.notify.TemplateList

/*
 * These tests verify that the templates we have in this code base match the templates stored in notify. This means
 * they need to query notify to retrieve the templates to compare. That requires the notify api key to be available
 * for these tests to be able to run.
 *
 * By default, these tests are disabled so that they don't fail when you don't have an environment variable set. To
 * run them locally, get the notify api key and set the appropriate environment variable on the gradle run
 * configuration. Under no circumstances should you commit the api key or configuration containing the api in git.
 * There is a prepared run configuration called "notify-template-tests.run.xml" that runs these tests - if you want
 * this ask your team lead where it can be found.
 */

@EnabledIf("canFetchNotifyTemplates")
@SpringBootTest(classes = [NotifyConfig::class])
class NotifyEmailTemplateTests {
    @Autowired
    private lateinit var notifyClient: NotificationClient

    companion object NotifyTestsCompanion {
        val jsonMetadataList = javaClass.getResource("/emails/emailTemplates.json")?.readText() ?: ""
        lateinit var notifyTemplates: TemplateList

        fun haveNotifyTemplatesBeenFetched() = ::notifyTemplates.isInitialized

        fun fetchNotifyTemplates(notifyClient: NotificationClient) {
            notifyTemplates = notifyClient.getAllTemplates("email")
        }

        @JvmStatic
        fun canFetchNotifyTemplates(): Boolean = System.getenv("EMAILNOTIFICATIONS_APIKEY") != null

        @JvmStatic
        fun getMetadataList() = Json.decodeFromString<List<EmailTemplateMetadata>>(jsonMetadataList)
    }

    @BeforeEach
    fun getNotifyTemplatesOnce() {
        if (!haveNotifyTemplatesBeenFetched()) {
            fetchNotifyTemplates(notifyClient)
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(EmailTemplateId::class)
    fun `notify contains a template for each template id`(id: EmailTemplateId) {
        notifyTemplates.templates.single { template -> template.id.toString() == id.idValue }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(EmailTemplateId::class)
    fun `there is a source controlled copy for each template id`(id: EmailTemplateId) {
        var metadataList = Json.decodeFromString<List<EmailTemplateMetadata>>(jsonMetadataList)

        metadataList.single { templateMetadata -> templateMetadata.id == id.idValue }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getMetadataList")
    fun `all source controlled templates match their notify equivalent`(metadata: EmailTemplateMetadata) {
        // Act
        var templateId = metadata.id
        var notifyTemplate = notifyTemplates.templates.single { template -> template.id.toString() == templateId }

        // Assert
        assertBodiesMatch(metadata, notifyTemplate)
        Assertions.assertEquals(metadata.name, notifyTemplate.name, "Notify template name did not match")
        Assertions.assertEquals(
            metadata.subject,
            notifyTemplate.subject.orElse(null),
            "Notify template subject did not match",
        )
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

        Assertions.assertEquals(cleanedStoredBody, cleanedNotifyBody, "Notify template body did not match")
    }

    @Serializable
    data class EmailTemplateMetadata(
        val id: String,
        val name: String,
        val subject: String,
        val bodyLocation: String,
    ) {
        override fun toString() = name
    }
}
