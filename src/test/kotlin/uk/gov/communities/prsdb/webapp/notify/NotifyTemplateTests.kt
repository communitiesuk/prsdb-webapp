package uk.gov.communities.prsdb.webapp.notify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.condition.EnabledIf
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplate
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.LetterTemplate
import uk.gov.communities.prsdb.webapp.testHelpers.NotifyEnvironment
import uk.gov.communities.prsdb.webapp.testHelpers.NotifyTemplateMetadata
import uk.gov.communities.prsdb.webapp.testHelpers.NotifyTemplateMetadataFactory
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.Template
import uk.gov.service.notify.TemplateList

/*
 * These tests verify that the email and letter templates we have in this code base match the templates stored in
 * notify. Every template is checked against both the integration and the production notify services, so that a
 * template which has only been updated in one of them is caught before it reaches production.
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
class NotifyTemplateTests {
    enum class NotifyTemplateType(
        val notifyName: String,
        val templateJsonPath: String,
        val templateNames: List<String>,
    ) {
        EMAIL("email", "/emails/emailTemplates.json", EmailTemplate.entries.map { it.name }),
        LETTER("letter", "/letters/letterTemplates.json", LetterTemplate.entries.map { it.name }),
    }

    companion object NotifyTestsCompanion {
        private val templatesByTypeAndEnvironment = mutableMapOf<Pair<NotifyTemplateType, NotifyEnvironment>, TemplateList>()

        private val metadataByTypeAndEnvironment =
            mutableMapOf<Pair<NotifyTemplateType, NotifyEnvironment>, List<NotifyTemplateMetadata>>()

        @JvmStatic
        fun canFetchNotifyTemplates(): Boolean = NotifyEnvironment.entries.any { it.isConfigured }

        @JvmStatic
        fun templateAndEnvironmentCombinations(): List<Arguments> =
            NotifyTemplateType.entries.flatMap { type ->
                type.templateNames.flatMap { templateName ->
                    NotifyEnvironment.entries.map { environment -> Arguments.of(type, templateName, environment) }
                }
            }

        fun notifyTemplates(
            type: NotifyTemplateType,
            environment: NotifyEnvironment,
        ): TemplateList =
            templatesByTypeAndEnvironment.getOrPut(type to environment) {
                NotificationClient(environment.apiKey!!).getAllTemplates(type.notifyName)
            }

        fun sourceControlledMetadata(
            type: NotifyTemplateType,
            environment: NotifyEnvironment,
        ): List<NotifyTemplateMetadata> =
            metadataByTypeAndEnvironment.getOrPut(type to environment) {
                NotifyTemplateMetadataFactory(environment, type.templateJsonPath).metadataList
            }
    }

    @ParameterizedTest(name = "{0} {1} in {2}")
    @MethodSource("templateAndEnvironmentCombinations")
    fun `notify contains a template for each template id`(
        type: NotifyTemplateType,
        templateName: String,
        environment: NotifyEnvironment,
    ) {
        assumeEnvironmentIsConfigured(environment)

        val metadata = sourceControlledMetadata(type, environment).singleOrNull { it.enumName == templateName }

        notifyTemplates(type, environment).templates.single { template -> template.id.toString() == metadata?.id }
    }

    @ParameterizedTest(name = "{0} {1} in {2}")
    @MethodSource("templateAndEnvironmentCombinations")
    fun `there is a source controlled copy for each template id`(
        type: NotifyTemplateType,
        templateName: String,
        environment: NotifyEnvironment,
    ) {
        val metadata = sourceControlledMetadata(type, environment).single { it.enumName == templateName }

        assertFalse(
            metadata.id.isNullOrBlank(),
            "${type.templateJsonPath} has no ${environment.templateIdJsonKey} for $templateName",
        )
    }

    @ParameterizedTest(name = "{0} {1} in {2}")
    @MethodSource("templateAndEnvironmentCombinations")
    fun `all source controlled templates match their notify equivalent`(
        type: NotifyTemplateType,
        templateName: String,
        environment: NotifyEnvironment,
    ) {
        // Arrange
        assumeEnvironmentIsConfigured(environment)
        val metadata = sourceControlledMetadata(type, environment).single { it.enumName == templateName }

        // Act
        val notifyTemplate = notifyTemplates(type, environment).templates.single { template -> template.id.toString() == metadata.id }

        // Assert
        assertBodiesMatch(metadata, notifyTemplate)
        // For letters, the subject is the letter heading
        assertEquals(
            metadata.subject,
            notifyTemplate.subject.orElse(null),
            "Notify template subject did not match",
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
