package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.transfer.s3.S3TransferManager
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository

@Service
class AwsS3FileDequarantiner(
    private val transferManager: S3TransferManager,
    private val s3Client: S3Client,
    @Value("\${aws.s3.quarantineBucket}")
    val quarantineBucketName: String,
    @Value("\${aws.s3.safeBucket}")
    val safeBucketName: String,
    private val fileUploadRepository: FileUploadRepository,
) : FileDequarantiner {
    override fun dequarantineFile(fileUpload: FileUpload): Boolean {
        val copyResponse =
            transferManager
                .copy { builder ->
                    builder.copyObjectRequest { requestBuilder ->
                        requestBuilder
                            .sourceBucket(quarantineBucketName)
                            .sourceKey(fileUpload.objectKey)
                            .destinationBucket(safeBucketName)
                            .destinationKey(fileUpload.objectKey)
                    }
                }.completionFuture()
                .join()
                .response()

        if (!copyResponse.sdkHttpResponse().isSuccessful) {
            return false
        }

        if (!deleteFile(fileUpload.objectKey)) {
            return false
        }

        fileUpload.status = FileUploadStatus.SCANNED
        fileUpload.eTag = copyResponse.copyObjectResult().eTag()
        fileUpload.versionId = copyResponse.versionId()
        fileUploadRepository.save(fileUpload)

        return true
    }

    override fun deleteFile(fileUpload: FileUpload): Boolean =
        if (deleteFile(fileUpload.objectKey)) {
            fileUploadRepository.delete(fileUpload)
            true
        } else {
            false
        }

    private fun deleteFile(objectKey: String): Boolean {
        val deleteResponse =
            s3Client
                .deleteObject { request ->
                    request.bucket(quarantineBucketName).key(objectKey)
                }

        return deleteResponse.sdkHttpResponse().isSuccessful
    }
}
