package uk.gov.communities.prsdb.webapp.application

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.communities.prsdb.webapp.database.entity.CertificateUpload
import uk.gov.communities.prsdb.webapp.database.repository.CertificateUploadRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult
import uk.gov.communities.prsdb.webapp.services.VirusScanProcessingService
import kotlin.system.exitProcess

@Component
@Profile("web-server-deactivated & scan-processor")
class ProcessScanResultApplicationRunner(
    private val context: ApplicationContext,
    private val service: VirusScanProcessingService,
    private val certificateUploadRepository: CertificateUploadRepository,
) : ApplicationRunner {
    @Value("\${SCAN_RESULT_STATUS:DEFAULT}")
    private lateinit var scanResultStatus: String

    @Value("\${S3_OBJECT_KEY:noObjectSet}")
    private lateinit var objectKey: String

    @Value("\${S3_QUARANTINE_BUCKET_KEY:noBucketSet}")
    private lateinit var eventBucketName: String

    @Value("\${S3_OBJECT_ETAG}")
    private lateinit var etag: String

    @Value("\${S3_OBJECT_VERSION_ID}")
    private lateinit var versionId: String

    @Value("\${aws.s3.quarantineBucket}")
    private lateinit var quarantineBucketName: String

    override fun run(args: ApplicationArguments?) {
        try {
            if (quarantineBucketName != eventBucketName) {
                throw PrsdbWebException("Invocation from scan on unexpected bucket: $eventBucketName")
            }

            val upload = getCertificateUpload()

            val scanStatus =
                ScanResult.fromStringValueOrNull(scanResultStatus)
                    ?: throw PrsdbWebException("Unknown guard duty status: $scanResultStatus")

            service.processScan(upload, scanStatus)

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

    private fun getCertificateUpload(): CertificateUpload {
        val certificateUpload =
            certificateUploadRepository.findByFileUpload_ObjectKeyAndFileUpload_VersionId(
                objectKey = objectKey,
                versionId = versionId,
            ) ?: throw PrsdbWebException("File upload not found for object key: $objectKey with version ID: $versionId")
        val fileETag = certificateUpload.fileUpload.eTag
        if (fileETag != etag) {
            throw PrsdbWebException("ETag mismatch for object key: $objectKey. Expected: $fileETag, Received: $etag")
        }
        return certificateUpload
    }
}
