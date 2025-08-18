package uk.gov.communities.prsdb.webapp.notify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.condition.EnabledIf
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import uk.gov.communities.prsdb.webapp.config.NotifyConfig
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplate
import uk.gov.communities.prsdb.webapp.testHelpers.EmailTemplateMetadata
import uk.gov.communities.prsdb.webapp.testHelpers.EmailTemplateMetadataFactory
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
@SpringBootTest(classes = [NotifyConfig::class, EmailTemplateMetadataFactory::class])
class NotifyEmailTemplateTests {
    @Autowired
    private lateinit var notifyClient: NotificationClient

    @Autowired
    private lateinit var emailTemplateMetadataFactory: EmailTemplateMetadataFactory

    companion object NotifyTestsCompanion {
        lateinit var notifyTemplates: TemplateList

        fun haveNotifyTemplatesBeenFetched() = ::notifyTemplates.isInitialized

        fun fetchNotifyTemplates(notifyClient: NotificationClient) {
            notifyTemplates = notifyClient.getAllTemplates("email")
        }

        @JvmStatic
        fun canFetchNotifyTemplates(): Boolean = System.getenv("EMAILNOTIFICATIONS_APIKEY") != null
    }

    @BeforeEach
    fun getNotifyTemplatesOnce() {
        if (!haveNotifyTemplatesBeenFetched()) {
            fetchNotifyTemplates(notifyClient)
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(EmailTemplate::class)
    fun `notify contains a template for each template id`(id: EmailTemplate) {
        val metadata =
            emailTemplateMetadataFactory.metadataList.singleOrNull { templateMetadata ->
                templateMetadata.enumName ==
                    id.name
            }

        notifyTemplates.templates.single { template -> template.id.toString() == metadata?.id }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(EmailTemplate::class)
    fun `there is a source controlled copy for each template id`(id: EmailTemplate) {
        val metadataList = emailTemplateMetadataFactory.metadataList

        metadataList.single { templateMetadata -> templateMetadata.enumName == id.name }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(EmailTemplate::class)
    fun `all source controlled templates match their notify equivalent`(id: EmailTemplate) {
        // Arrange
        val metadata = emailTemplateMetadataFactory.metadataList.single { templateMetadata -> templateMetadata.enumName == id.name }

        // Act
        var templateId = metadata.id
        var notifyTemplate = notifyTemplates.templates.single { template -> template.id.toString() == templateId }

        // Assert
        assertBodiesMatch(metadata, notifyTemplate)
        assertEquals(
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

        assertEquals(cleanedStoredBody, cleanedNotifyBody, "Notify template body did not match")
    }
}
