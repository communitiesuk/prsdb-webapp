package uk.gov.communities.prsdb.webapp.examples

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.ExampleEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import kotlin.system.exitProcess

@Component
@Profile("web-server-deactivated & example-scan-processor")
class ExampleScanProcessingApplicationRunner(
    private val emailSender: EmailNotificationService<ExampleEmail>,
    private val context: ApplicationContext,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
) : ApplicationRunner {
    @Value("\${SCAN_RESULT_STATUS:DEFAULT}")
    lateinit var scanResultStatus: String

    @Value("\${S3_OBJECT_KEY:noObjectSet")
    lateinit var objectKey: String

    override fun run(args: ApplicationArguments?) {
        val ownershipId = getPropertyOwnershipIdOrNull(objectKey)
        val ownership = ownershipId?.let { propertyOwnershipRepository.findByIdAndIsActiveTrue(it) }

        if (ownership == null) {
            emailSender.sendEmail(
                "team-prsdb+unowned-scan-result@softwire.com",
                ExampleEmail("No ownership for file $objectKey"),
            )
        } else {
            emailSender.sendEmail(
                ownership.primaryLandlord.email,
                ExampleEmail(scanResultStatus),
            )
        }

        val code =
            SpringApplication.exit(context, { 0 }).also {
                println("Example email sent successfully. Application will exit now.")
            }
        exitProcess(code)
    }

    private fun getPropertyOwnershipIdOrNull(fileName: String): Long? {
        val parts = fileName.split("_")
        if (parts.size < 3) {
            return null
        }
        val propertyOwnershipIdPart = parts[1]
        return propertyOwnershipIdPart.toLongOrNull()
    }
}
