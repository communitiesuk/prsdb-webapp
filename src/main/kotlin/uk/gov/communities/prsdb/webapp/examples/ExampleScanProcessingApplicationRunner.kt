package uk.gov.communities.prsdb.webapp.examples

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.GuardDutyScanResult
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
    @Value("\${SCAN_RESULT:{\"scanStatus\": \"DEFAULT\"}}")
    lateinit var scanResultJson: String

    override fun run(args: ApplicationArguments?) {
        val scanResult: GuardDutyScanResult = GuardDutyScanResult.fromJson(scanResultJson)

        val ownershipId = getPropertyOwnershipIdOrNull(scanResult.s3ObjectDetails?.objectKey ?: "")
        val ownership = ownershipId?.let { propertyOwnershipRepository.findByIdAndIsActiveTrue(it) }

        if (ownership == null) {
            emailSender.sendEmail(
                "team-prsdb+unowned-scan-result@softwire.com",
                ExampleEmail("No ownership for file ${scanResult.s3ObjectDetails?.objectKey}"),
            )
        } else {
            emailSender.sendEmail(
                ownership.primaryLandlord.email,
                ExampleEmail(scanResult.scanResultDetails?.scanResultStatus ?: ""),
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
