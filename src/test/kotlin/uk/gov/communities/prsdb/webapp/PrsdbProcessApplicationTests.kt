package uk.gov.communities.prsdb.webapp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@ActiveProfiles("web-server-deactivated", "local")
// The EMAILNOTIFICATIONS_APIKEY property is required for the Notify config to load, and even with no value set the beans can be created
@TestPropertySource(properties = ["EMAILNOTIFICATIONS_APIKEY"])
class PrsdbProcessApplicationTests {
    @Autowired
    private var context: ConfigurableApplicationContext? = null

    @Test
    fun `only necessary PRSDB beans are available in non web mode`() {
        val expectedBeanMap =
            mapOf(
                "prsdbWebappApplication" to "uk.gov.communities.prsdb.webapp.PrsdbWebappApplication",
                "notifyConfig" to "uk.gov.communities.prsdb.webapp.config.NotifyConfig$\$SpringCGLIB$$0",
                "uk.gov.communities.prsdb.webapp.TestcontainersConfiguration" to
                    "uk.gov.communities.prsdb.webapp.TestcontainersConfiguration",
                "emailNotificationStubService" to "uk.gov.communities.prsdb.webapp.local.services.EmailNotificationStubService",
                "notifyEmailNotificationService" to "uk.gov.communities.prsdb.webapp.services.NotifyEmailNotificationService",
            )

        val beanNameAndClassMap =
            context!!
                .beanDefinitionNames
                .mapNotNull { name -> (name to context!!.beanFactory.getBeanDefinition(name).beanClassName) }
                .filter { it.second?.contains("uk.gov.communities.prsdb.webapp") ?: false }
                .associate { it }

        assertEquals(
            expectedBeanMap,
            beanNameAndClassMap,
        ) { buildHelpfulErrorMessage(expectedBeanMap, beanNameAndClassMap) }
    }

    fun buildHelpfulErrorMessage(
        expected: Map<String, String>,
        actual: Map<String, String?>,
    ): String {
        val expectedBeansMissing =
            expected.keys.filter { !actual.containsKey(it) }
        val unexpectedBeans =
            actual.keys.filter { !expected.containsKey(it) }

        val combinedMessage =
            if (expectedBeansMissing.isNotEmpty() && unexpectedBeans.isNotEmpty()) {
                """There are beans missing that are expected and unexpected beans present in non web server mode.
                |This could be because some beans have been renamed, or there may be multiple problems that need addressing:
                """.trimMargin()
            } else {
                null
            }
        val missingBeansMessage =
            if (expectedBeansMissing.isNotEmpty()) {
                """The following beans expected for non web server mode are missing - check they haven't been changed to web only or removed (or remove them from the expected list):
                |$expectedBeansMissing
                """.trimMargin()
            } else {
                null
            }
        val unexpectedBeansMessage =
            if (unexpectedBeans.isNotEmpty()) {
                """The following beans are being created in non web server mode but have not been added to the expected list. 
                |For convenience they are formatted as map entries so IF RELEVANT can be copied and pasted into the expected map:
                |${
                    unexpectedBeans.joinToString(",\n") { missingName ->
                        "\"$missingName\" to \"${actual[missingName]}\""
                    }}
                |You may need to escape special characters like $.
                """.trimMargin()
            } else {
                null
            }
        return listOfNotNull(
            combinedMessage,
            missingBeansMessage,
            unexpectedBeansMessage,
        ).joinToString("\n\n")
    }
}
