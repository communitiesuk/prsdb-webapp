package uk.gov.communities.prsdb.webapp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.transfer.s3.S3TransferManager
import uk.gov.communities.prsdb.webapp.config.NotifyConfig
import uk.gov.communities.prsdb.webapp.config.S3Config
import uk.gov.communities.prsdb.webapp.local.services.EmailNotificationStubService
import uk.gov.communities.prsdb.webapp.local.services.LocalDequarantiningFileCopier
import uk.gov.communities.prsdb.webapp.local.services.LocalQuarantinedFileDeleter
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.AwsS3DequarantiningFileCopier
import uk.gov.communities.prsdb.webapp.services.AwsS3QuarantinedFileDeleter
import uk.gov.communities.prsdb.webapp.services.NotifyEmailNotificationService
import uk.gov.communities.prsdb.webapp.services.UploadDequarantiner
import uk.gov.communities.prsdb.webapp.services.VirusAlertSender
import uk.gov.communities.prsdb.webapp.services.VirusScanProcessingService
import kotlin.jvm.java
import kotlin.reflect.KClass

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@ActiveProfiles("web-server-deactivated", "local")
// The EMAILNOTIFICATIONS_APIKEY property is required for the Notify config to load, and even with no value set the beans can be created
@TestPropertySource(properties = ["EMAILNOTIFICATIONS_APIKEY"])
class PrsdbProcessApplicationTests {
    @Autowired
    private var context: ConfigurableApplicationContext? = null

    @MockitoBean
    lateinit var s3: S3TransferManager

    @MockitoBean
    lateinit var s3client: S3Client

    @Test
    fun `only necessary PRSDB beans are available in non web mode`() {
        val expectedBeansByName =
            listOf(
                // Beans added by component annotation scanning use their simple name as the bean name by default
                PrsdbWebappApplication::class.java.simpleName,
                EmailNotificationStubService::class.java.simpleName,
                NotifyEmailNotificationService::class.java.simpleName,
                NotifyConfig::class.java.simpleName,
                S3Config::class.java.simpleName,
                AwsS3QuarantinedFileDeleter::class.java.simpleName,
                LocalQuarantinedFileDeleter::class.java.simpleName,
                AwsS3DequarantiningFileCopier::class.java.simpleName,
                LocalDequarantiningFileCopier::class.java.simpleName,
                UploadDequarantiner::class.java.simpleName,
                VirusScanProcessingService::class.java.simpleName,
                AbsoluteUrlProvider::class.java.simpleName,
                VirusAlertSender::class.java.simpleName,
                // Beans with explicit names can retrieve their name by reflecting on the annotation using the `getExplicitBeanName` function
                // e.g. getExplicitBeanName<Component>(YourComponentClassName::class),
                // Beans added by @Import annotations use their fully qualified class name as the bean name by default
                TestcontainersConfiguration::class.java.name,
            ).map { it.lowercase() }.toSet()

        val beanNames =
            context!!
                .beanDefinitionNames
                .filter {
                    context!!
                        .beanFactory
                        .getBeanDefinition(it)
                        .beanClassName
                        ?.contains("uk.gov.communities.prsdb.webapp") ?: false
                }.map { it.lowercase() }
                .toSet()

        assertEquals(expectedBeansByName, beanNames) {
            buildHelpfulErrorMessage(
                expectedBeansByName.toList(),
                beanNames.toList(),
            )
        }
    }

    private inline fun <reified TAnnotation : Annotation> getExplicitBeanName(klass: KClass<*>): String =
        when (TAnnotation::class) {
            Component::class -> klass.java.getAnnotation(Component::class.java)?.value
            Service::class -> klass.java.getAnnotation(Service::class.java)?.value
            Configuration::class -> klass.java.getAnnotation(Configuration::class.java)?.value
            else -> throw IllegalArgumentException("Unsupported annotation type: ${TAnnotation::class}")
        } ?: throw IllegalArgumentException("${TAnnotation::class} present on ${klass.simpleName} but no explicit name provided")

    fun buildHelpfulErrorMessage(
        expectedBeans: List<String>,
        actualBeans: List<String>,
    ): String {
        val missingBeans = expectedBeans.filter { !actualBeans.contains(it) }
        val unexpectedBeans = actualBeans.filter { !expectedBeans.contains(it) }

        val combinedMessage =
            if (missingBeans.isNotEmpty() && unexpectedBeans.isNotEmpty()) {
                """There are beans missing that are expected and unexpected beans present in non web server mode.
                |This could be because some beans have been renamed, or there may be multiple problems that need addressing:
                """.trimMargin()
            } else {
                null
            }
        val missingBeansMessage =
            if (missingBeans.isNotEmpty()) {
                """The following beans expected for non web server mode are missing - check they haven't been changed to web only or removed (or remove them from the expected list):
                |${missingBeans.joinToString("\n- ", prefix = "- ")}
                """.trimMargin()
            } else {
                null
            }
        val unexpectedBeansMessage =
            if (unexpectedBeans.isNotEmpty()) {
                """The following beans are being created in non web server mode but have not been added to the expected list: 
                |${unexpectedBeans.joinToString("\n- ", prefix = "- ")}
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
