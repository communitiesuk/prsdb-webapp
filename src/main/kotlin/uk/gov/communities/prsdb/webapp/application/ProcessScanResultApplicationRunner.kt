package uk.gov.communities.prsdb.webapp.application

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyFileNameInfo
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult
import uk.gov.communities.prsdb.webapp.services.VirusScanProcessingService
import kotlin.system.exitProcess

@Component
@Profile("web-server-deactivated & scan-processor")
class ProcessScanResultApplicationRunner(
    private val context: ApplicationContext,
    private val service: VirusScanProcessingService,
) : ApplicationRunner {
    @Value("\${SCAN_RESULT_STATUS:DEFAULT}")
    private lateinit var scanResultStatus: String

    @Value("\${S3_OBJECT_KEY:noObjectSet}")
    private lateinit var objectKey: String

    @Value("\${S3_QUARANTINE_BUCKET_KEY:noBucketSet}")
    private lateinit var eventBucketName: String

    @Value("\${aws.s3.quarantineBucket}")
    private lateinit var quarantineBucketName: String

    override fun run(args: ApplicationArguments?) {
        try {
            if (quarantineBucketName != eventBucketName) {
                throw PrsdbWebException("Invocation from scan on unexpected bucket: $eventBucketName")
            }

            val fileNameInfo = PropertyFileNameInfo.parse(objectKey)
            val scanStatus =
                ScanResult.fromStringValueOrNull(scanResultStatus)
                    ?: throw PrsdbWebException("Unknown guard duty status: $scanResultStatus")

            service.processScan(fileNameInfo, scanStatus)

            val code =
                SpringApplication.exit(context, { 0 }).also {
                    println("Virus scan result processed successfully. Application will exit now.")
                }
            exitProcess(code)
        } catch (prsdbWebException: PrsdbWebException) {
            println("Error processing scan result: ${prsdbWebException.message}")
            throw prsdbWebException
        }
    }
}
