package uk.gov.communities.prsdb.webapp.models.viewModels.letterModels

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.services.NotifyLetterNotificationService
import uk.gov.communities.prsdb.webapp.testHelpers.NotifyTemplateMetadataFactory

class LetterTemplateModelsTests {
    companion object {
        @JvmStatic
        private fun templateList() =
            listOf(
                LetterTemplateTestData(ExampleLetter("Jill"), "/letters/ExampleLetter.md"),
            )
    }

    data class LetterTemplateTestData(
        val model: LetterTemplateModel,
        val markdownLocation: String,
    ) {
        override fun toString(): String = model.javaClass.simpleName
    }

    @ParameterizedTest(name = "{0} keys match markdown")
    @MethodSource("templateList")
    fun `LetterTemplateModels hashmaps have keys that match the parameters in their markdown templates`(testData: LetterTemplateTestData) {
        // Arrange
        val letterTemplateMetadata = NotifyTemplateMetadataFactory(null, "/letters/letterTemplates.json")
        val storedBody = javaClass.getResource(testData.markdownLocation)?.readText() ?: ""
        val storedMetadata =
            letterTemplateMetadata.metadataList.single { metadata ->
                metadata.enumName == testData.model.template.name
            }

        val subjectParameters = extractParameters(storedMetadata.subject)
        val bodyParameters = extractParameters(storedBody)
        val parameters = (subjectParameters + bodyParameters).distinct()

        // Act
        val modelHashMap = testData.model.toHashMap()

        // Assert
        Assertions.assertEquals(parameters.toSet(), modelHashMap.keys)
    }

    @ParameterizedTest(name = "{0} keys are not reserved address keys")
    @MethodSource("templateList")
    fun `LetterTemplateModels hashmaps do not have keys that clash with the recipient address`(testData: LetterTemplateTestData) {
        // Act
        val modelHashMap = testData.model.toHashMap()

        // Assert
        val reservedKeys = modelHashMap.keys.filter { NotifyLetterNotificationService.isReservedAddressKey(it) }
        Assertions.assertTrue(reservedKeys.isEmpty(), "Reserved address keys found: $reservedKeys")
    }

    private fun extractParameters(body: String): List<String> {
        val parameterRegex = Regex("\\(\\((.*?)\\)\\)")
        return parameterRegex.findAll(body).map { result -> result.value.trim(')').trim('(') }.toList()
    }
}
