package uk.gov.communities.prsdb.webapp.notify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.condition.EnabledIf
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplate
import uk.gov.communities.prsdb.webapp.testHelpers.EmailTemplateMetadata
import uk.gov.communities.prsdb.webapp.testHelpers.EmailTemplateMetadataFactory
import uk.gov.communities.prsdb.webapp.testHelpers.NotifyEnvironment
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.Template
import uk.gov.service.notify.TemplateList

/*
 * These tests verify that the templates we have in this code base match the templates stored in notify. Every
 * template is checked against both the integration and the production notify services, so that a template which has
 * only been updated in one of them is caught before it reaches production.
 *
 * Each notify service needs its own api key: EMAILNOTIFICATIONS_APIKEY for integration and
 * EMAILNOTIFICATIONS_PRODUCTION_APIKEY for production. A service whose key is not set is skipped, and these tests
 * are disabled entirely when neither key is set, so that they don't fail when you have no keys available. To run
 * them locally, get the keys and set the appropriate environment variables on the gradle run configuration. Under
 * no circumstances should you commit an api key or configuration containing one in git. There is a prepared run
 * configuration called "notify-template-tests.run.xml" that runs these tests - if you want this ask your team lead
 * where it can be found.
 */

@EnabledIf("canFetchNotifyTemplates")
class NotifyEmailTemplateTests {
    companion object NotifyTestsCompanion {
        private val templatesByEnvironment = mutableMapOf<NotifyEnvironment, TemplateList>()

        private val metadataByEnvironment = mutableMapOf<NotifyEnvironment, List<EmailTemplateMetadata>>()

        @JvmStatic
        fun canFetchNotifyTemplates(): Boolean = NotifyEnvironment.entries.any { it.isConfigured }

        @JvmStatic
        fun templateAndEnvironmentCombinations(): List<Arguments> =
            EmailTemplate.entries.flatMap { template ->
                NotifyEnvironment.entries.map { environment -> Arguments.of(template, environment) }
            }

        fun notifyTemplates(environment: NotifyEnvironment): TemplateList =
            templatesByEnvironment.getOrPut(environment) {
                NotificationClient(environment.apiKey!!).getAllTemplates("email")
            }

        fun sourceControlledMetadata(environment: NotifyEnvironment): List<EmailTemplateMetadata> =
            metadataByEnvironment.getOrPut(environment) {
                EmailTemplateMetadataFactory(environment).metadataList
            }
    }

    @ParameterizedTest(name = "{0} in {1}")
    @MethodSource("templateAndEnvironmentCombinations")
    fun `notify contains a template for each template id`(
        id: EmailTemplate,
        environment: NotifyEnvironment,
    ) {
        assumeEnvironmentIsConfigured(environment)

        val metadata =
            sourceControlledMetadata(environment).singleOrNull { templateMetadata ->
                templateMetadata.enumName ==
                    id.name
            }

        notifyTemplates(environment).templates.single { template -> template.id.toString() == metadata?.id }
    }

    @ParameterizedTest(name = "{0} in {1}")
    @MethodSource("templateAndEnvironmentCombinations")
    fun `there is a source controlled copy for each template id`(
        id: EmailTemplate,
        environment: NotifyEnvironment,
    ) {
        val metadata = sourceControlledMetadata(environment).single { templateMetadata -> templateMetadata.enumName == id.name }

        assertFalse(
            metadata.id.isNullOrBlank(),
            "emailTemplates.json has no ${environment.templateIdJsonKey} for ${id.name}",
        )
    }

    @ParameterizedTest(name = "{0} in {1}")
    @MethodSource("templateAndEnvironmentCombinations")
    fun `all source controlled templates match their notify equivalent`(
        id: EmailTemplate,
        environment: NotifyEnvironment,
    ) {
        // Arrange
        assumeEnvironmentIsConfigured(environment)
        val metadata = sourceControlledMetadata(environment).single { templateMetadata -> templateMetadata.enumName == id.name }

        // Act
        val notifyTemplate = notifyTemplates(environment).templates.single { template -> template.id.toString() == metadata.id }

        // Assert
        assertBodiesMatch(metadata, notifyTemplate)
        assertEquals(
            metadata.subject,
            notifyTemplate.subject.orElse(null),
            "Notify template subject did not match",
        )
    }

    private fun assumeEnvironmentIsConfigured(environment: NotifyEnvironment) =
        assumeTrue(environment.isConfigured, "No notify api key configured for $environment")

    private fun assertBodiesMatch(
        metadata: EmailTemplateMetadata,
        notifyTemplate: Template,
    ) {
        val storedBody = javaClass.getResource(metadata.bodyLocation)?.readText() ?: ""

        // We don't care about line ending types: convert to LF before comparison, and trim leading/trailing newlines
        val cleanedStoredBody = storedBody.replace("\r", "").trim('\n')
        val notifyBody = notifyTemplate.body

        // Notify returns body with CRLF end lines: convert to LF before comparison, and trim leading/trailing newlines
        val cleanedNotifyBody = notifyBody.replace("\r", "").trim('\n')

        assertEquals(cleanedStoredBody, cleanedNotifyBody, "Notify template body did not match")
    }
}
