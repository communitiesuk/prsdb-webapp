package uk.gov.communities.prsdb.webapp.models.viewModels

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import java.net.URI

class EmailTemplateModelsTests {
    companion object {
        @JvmStatic
        private fun templateList() =
            listOf(
                EmailTemplateTestData(ExampleEmail("test string"), "/emails/ExampleEmail.md"),
                EmailTemplateTestData(
                    LocalAuthorityInvitationEmail(createLocalAuthority(1, "name"), URI("https://example.com")),
                    "/emails/LocalAuthorityInvitation.md",
                ),
                EmailTemplateTestData(
                    LocalAuthorityInvitationCancellationEmail(createLocalAuthority(1, "name")),
                    "/emails/LocalAuthorityInvitationCancellation.md",
                ),
            )

        private fun createLocalAuthority(
            id: Int,
            name: String,
        ): LocalAuthority {
            val localAuthority = LocalAuthority()
            ReflectionTestUtils.setField(localAuthority, "id", id)
            ReflectionTestUtils.setField(localAuthority, "name", name)

            return localAuthority
        }
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
        var parameters = getParametersFromBody(storedBody).distinct()

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
