package uk.gov.communities.prsdb.webapp.models.viewModels.letterModels

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.services.NotifyLetterNotificationService
import uk.gov.communities.prsdb.webapp.testHelpers.NotifyTemplateMetadataFactory

class LetterTemplateModelsTests {
    companion object {
        @JvmStatic
        private fun templateList() =
            listOf(
                LetterTemplateTestData(
                    FreeholderAddedToPropertyLetter(
                        freeholderName = "Sarah Thompson",
                        propertyAddress = "12 Real Street\nBristol\nBS8 1AA",
                        landlords = listOf("Jamie Jameson (jamie@example.com)"),
                        freeholderAddress = "18 Market Street\nBristol\nBS1 4DJ",
                        privacyNoticeUrl = "example.test/landlord/privacy-notice",
                    ),
                    "/letters/FreeholderAddedToPropertyLetter.md",
                ),
                LetterTemplateTestData(
                    SuperiorLandlordAddedToPropertyLetter(
                        superiorLandlordName = "Daniel Roberts",
                        propertyAddress = "12 Real Street\nBristol\nBS8 1AA",
                        landlords = listOf("Jamie Jameson (jamie@example.com)"),
                        superiorLandlordAddress = "18 Market Street\nBristol\nBS1 4DJ",
                        privacyNoticeUrl = "example.test/landlord/privacy-notice",
                    ),
                    "/letters/SuperiorLandlordAddedToPropertyLetter.md",
                ),
            )
    }

    data class LetterTemplateTestData(
        val model: LetterTemplateModel,
        val markdownLocation: String,
    ) {
        override fun toString(): String = model.javaClass.simpleName
    }

    @Test
    fun `Freeholder added to property letter formats landlords as bullets and preserves multiline addresses`() {
        val letter =
            FreeholderAddedToPropertyLetter(
                freeholderName = "Sarah Thompson",
                propertyAddress = "12 Real Street\nBristol\nBS8 1AA",
                landlords = listOf("Jamie Jameson (jamie@example.com)", "Tommy Cat (tommy@example.com)"),
                freeholderAddress = "18 Market Street\nBristol\nBS1 4DJ",
                privacyNoticeUrl = "example.test/landlord/privacy-notice",
            )

        val personalisation = letter.toHashMap()

        Assertions.assertEquals(
            "* Jamie Jameson (jamie@example.com)\n* Tommy Cat (tommy@example.com)",
            personalisation["landlords"],
        )
        Assertions.assertEquals("12 Real Street\nBristol\nBS8 1AA", personalisation["property address"])
        Assertions.assertEquals("18 Market Street\nBristol\nBS1 4DJ", personalisation["freeholder address"])
        Assertions.assertEquals("example.test/landlord/privacy-notice", personalisation["privacy notice"])
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

        val subjectParameters = storedMetadata.subject?.let(::extractParameters).orEmpty()
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
