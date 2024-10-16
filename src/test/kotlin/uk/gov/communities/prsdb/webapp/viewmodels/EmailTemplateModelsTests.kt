package uk.gov.communities.prsdb.webapp.viewmodels
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.viewmodel.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.viewmodel.ExampleEmail

class EmailTemplateModelsTests {
    companion object {
        @JvmStatic
        private fun templateList() =
            listOf(
                EmailTemplateTestData(ExampleEmail("test string"), "/emails/ExampleEmail.md"),
            )
    }

    data class EmailTemplateTestData(
        val model: EmailTemplateModel,
        val markdownLocation: String,
    ) {
        override fun toString(): String = model.javaClass.simpleName
    }

    @ParameterizedTest(name = "{0} keys match markdown")
    @MethodSource("templateList")
    fun `EmailTemplateModels hashmaps have keys that match the parameters in their markdown templates`(testData: EmailTemplateTestData) {
        // Arrange
        var storedBody = javaClass.getResource(testData.markdownLocation)?.readText() ?: ""
        var parameters = getParametersFromBody(storedBody)

        // Act
        var modelHashMap = testData.model.toHashMap()

        // Assert
        Assertions.assertEquals(parameters.size, modelHashMap.size)
        for (parameter in parameters) {
            modelHashMap.keys.single { key -> key == parameter }
        }
    }

    private fun getParametersFromBody(body: String): List<String> {
        val parameterRegex = Regex("\\(\\(.*\\)\\)")
        return parameterRegex.findAll(body).map { result -> result.value.trim(')').trim('(') }.toList()
    }
}
