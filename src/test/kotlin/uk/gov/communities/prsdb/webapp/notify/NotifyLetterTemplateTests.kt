package uk.gov.communities.prsdb.webapp.notify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.condition.EnabledIf
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.LetterTemplate
import uk.gov.communities.prsdb.webapp.testHelpers.NotifyEnvironment
import uk.gov.communities.prsdb.webapp.testHelpers.NotifyTemplateMetadata
import uk.gov.communities.prsdb.webapp.testHelpers.NotifyTemplateMetadataFactory
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.Template
import uk.gov.service.notify.TemplateList

/*
 * These tests verify that the letter templates in this code base match the letter templates stored in notify. They work
 * in the same way as NotifyEmailTemplateTests, using the same api keys, and are disabled when neither key is set.
 * Keep the two classes in step - see NotifyEmailTemplateTests for details of the api keys and running these locally.
 */

@EnabledIf("canFetchNotifyTemplates")
class NotifyLetterTemplateTests {
    companion object NotifyTestsCompanion {
        private val templatesByEnvironment = mutableMapOf<NotifyEnvironment, TemplateList>()

        private val metadataByEnvironment = mutableMapOf<NotifyEnvironment, List<NotifyTemplateMetadata>>()

        @JvmStatic
        fun canFetchNotifyTemplates(): Boolean = NotifyEnvironment.entries.any { it.isConfigured }

        @JvmStatic
        fun templateAndEnvironmentCombinations(): List<Arguments> =
            LetterTemplate.entries.flatMap { template ->
                NotifyEnvironment.entries.map { environment -> Arguments.of(template, environment) }
            }

        fun notifyTemplates(environment: NotifyEnvironment): TemplateList =
            templatesByEnvironment.getOrPut(environment) {
                NotificationClient(environment.apiKey!!).getAllTemplates("letter")
            }

        fun sourceControlledMetadata(environment: NotifyEnvironment): List<NotifyTemplateMetadata> =
            metadataByEnvironment.getOrPut(environment) {
                NotifyTemplateMetadataFactory(environment, "/letters/letterTemplates.json").metadataList
            }
    }

    @ParameterizedTest(name = "{0} in {1}")
    @MethodSource("templateAndEnvironmentCombinations")
    fun `notify contains a template for each template id`(
        id: LetterTemplate,
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
        id: LetterTemplate,
        environment: NotifyEnvironment,
    ) {
        val metadata = sourceControlledMetadata(environment).single { templateMetadata -> templateMetadata.enumName == id.name }

        assertFalse(
            metadata.id.isNullOrBlank(),
            "letterTemplates.json has no ${environment.templateIdJsonKey} for ${id.name}",
        )
    }

    @ParameterizedTest(name = "{0} in {1}")
    @MethodSource("templateAndEnvironmentCombinations")
    fun `all source controlled templates match their notify equivalent`(
        id: LetterTemplate,
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
            "Notify letter template heading did not match",
        )
    }

    private fun assumeEnvironmentIsConfigured(environment: NotifyEnvironment) =
        assumeTrue(environment.isConfigured, "No notify api key configured for $environment")

    private fun assertBodiesMatch(
        metadata: NotifyTemplateMetadata,
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
