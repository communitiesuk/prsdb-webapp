package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3control.S3ControlClient
import software.amazon.awssdk.services.s3control.model.CreateJobRequest
import software.amazon.awssdk.services.s3control.model.DescribeJobRequest
import software.amazon.awssdk.services.s3control.model.JobManifest
import software.amazon.awssdk.services.s3control.model.JobManifestFieldName
import software.amazon.awssdk.services.s3control.model.JobManifestFormat
import software.amazon.awssdk.services.s3control.model.JobManifestLocation
import software.amazon.awssdk.services.s3control.model.JobManifestSpec
import software.amazon.awssdk.services.s3control.model.JobOperation
import software.amazon.awssdk.services.s3control.model.JobReport
import software.amazon.awssdk.services.s3control.model.JobStatus
import software.amazon.awssdk.services.s3control.model.S3CopyObjectOperation
import software.amazon.awssdk.transfer.s3.S3TransferManager
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.time.LocalDateTime
import java.util.UUID

@PrsdbTaskService
class NftFileUploader(
    private val transferManager: S3TransferManager,
    private val s3ControlClient: S3ControlClient,
    @Value("\${aws.s3.bucketName}")
    private val bucketName: String,
    @Value("\${aws.accountId}")
    private val accountId: String,
    @Value("\${aws.s3.roleArn}")
    private val roleArn: String,
) {
    private lateinit var localManifestFile: File
    private lateinit var localManifestFileWriter: BufferedWriter

    fun addFileToManifest(objectKey: String) {
        if (!::localManifestFileWriter.isInitialized) {
            localManifestFile = File.createTempFile("nft-file-uploader-manifest-", ".csv")
            localManifestFileWriter = localManifestFile.bufferedWriter(Charsets.UTF_8)
        }
        localManifestFileWriter.write("$bucketName,$objectKey")
        localManifestFileWriter.newLine()
    }

    fun uploadFilesInManifest() {
        if (!::localManifestFile.isInitialized) {
            log("No files were added to the manifest, skipping upload")
            return
        }

        try {
            localManifestFileWriter.close()
            uploadSourceFile()
            val manifestETag = uploadManifestReturningETag()
            val jobId = submitBatchCopyJobReturningJobId(manifestETag)
            pollJobUntilComplete(jobId)
        } finally {
            localManifestFile.delete()
        }
    }

    private fun uploadSourceFile() {
        val templateFile = File(TEMPLATE_FILE_LOCATION)

        if (!templateFile.exists()) throw FileNotFoundException("Template file not found at $TEMPLATE_FILE_LOCATION")

        val uploadResponse =
            transferManager
                .uploadFile { builder ->
                    builder
                        .source(templateFile)
                        .putObjectRequest { request ->
                            request
                                .bucket(bucketName)
                                .key(SOURCE_OBJECT_KEY)
                        }
                }.completionFuture()
                .join()
                .response()
                .sdkHttpResponse()

        if (!uploadResponse.isSuccessful) throw S3Exception.builder().message("Failed to upload template file").build()
    }

    private fun uploadManifestReturningETag(): String {
        log("Starting to upload manifest file")

        val uploadResponse =
            transferManager
                .uploadFile { builder ->
                    builder
                        .source(localManifestFile)
                        .putObjectRequest { request ->
                            request
                                .bucket(bucketName)
                                .key(MANIFEST_OBJECT_KEY)
                                .contentType("text/csv")
                        }
                }.completionFuture()
                .join()
                .response()

        if (!uploadResponse.sdkHttpResponse().isSuccessful) throw S3Exception.builder().message("Failed to upload manifest").build()

        log("Finished uploading manifest file")

        return uploadResponse.eTag()
    }

    private fun submitBatchCopyJobReturningJobId(manifestETag: String): String {
        log("Started submitting upload job")

        val jobManifest =
            JobManifest
                .builder()
                .spec(
                    JobManifestSpec
                        .builder()
                        .format(JobManifestFormat.S3_BATCH_OPERATIONS_CSV_20180820)
                        .fields(JobManifestFieldName.BUCKET, JobManifestFieldName.KEY)
                        .build(),
                ).location(
                    JobManifestLocation
                        .builder()
                        .objectArn("arn:aws:s3:::$bucketName/$MANIFEST_OBJECT_KEY")
                        .eTag(manifestETag)
                        .build(),
                ).build()

        val jobOperation =
            JobOperation
                .builder()
                .s3PutObjectCopy(
                    S3CopyObjectOperation
                        .builder()
                        .targetResource("arn:aws:s3:::$bucketName")
                        .redirectLocation("/$SOURCE_OBJECT_KEY")
                        .build(),
                ).build()

        val jobReport =
            JobReport
                .builder()
                .enabled(false)
                .build()

        val createJobRequest =
            CreateJobRequest
                .builder()
                .manifest(jobManifest)
                .operation(jobOperation)
                .report(jobReport)
                .accountId(accountId)
                .roleArn(roleArn)
                .confirmationRequired(false)
                .priority(10)
                .clientRequestToken(UUID.randomUUID().toString())
                .build()

        val response = s3ControlClient.createJob(createJobRequest)

        log("Finished submitting upload job")

        return response.jobId()
    }

    private fun pollJobUntilComplete(jobId: String) {
        log("Starting to poll upload job until complete")

        val describeJobRequest =
            DescribeJobRequest
                .builder()
                .accountId(accountId)
                .jobId(jobId)
                .build()

        do {
            Thread.sleep(POLL_INTERVAL_MS)

            val job = s3ControlClient.describeJob(describeJobRequest).job()
            val status = job.status()
            val progress = job.progressSummary()

            log(
                "Upload job status: $status, " +
                    "succeeded: ${progress?.numberOfTasksSucceeded() ?: 0}, " +
                    "failed: ${progress?.numberOfTasksFailed() ?: 0}, " +
                    "total: ${progress?.totalNumberOfTasks() ?: 0}",
            )
        } while (status !in terminalStatuses)

        log("Upload job complete")
    }

    private fun log(message: String) {
        println("${LocalDateTime.now()} $message")
    }

    companion object {
        private const val TEMPLATE_FILE_LOCATION = "src/main/resources/data/certificates/certificate_template.png"
        private const val SOURCE_OBJECT_KEY = "seeding/certificate_template.png"
        private const val MANIFEST_OBJECT_KEY = "seeding/manifest.csv"
        private const val POLL_INTERVAL_MS = 30_000L

        private val terminalStatuses =
            setOf(
                JobStatus.COMPLETE,
                JobStatus.FAILED,
                JobStatus.CANCELLED,
            )
    }
}
