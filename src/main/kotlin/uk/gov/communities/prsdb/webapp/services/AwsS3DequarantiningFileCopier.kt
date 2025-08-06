package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.transfer.s3.S3TransferManager
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.models.dataModels.UploadedFileLocator

@Service
class AwsS3DequarantiningFileCopier(
    private val transferManager: S3TransferManager,
    @Value("\${aws.s3.quarantineBucket}")
    val quarantineBucketName: String,
    @Value("\${aws.s3.safeBucket}")
    val safeBucketName: String,
) : DequarantiningFileCopier {
    override fun copyFile(fileUpload: FileUpload): UploadedFileLocator? {
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
            return null
        }

        return UploadedFileLocator(
            objectKey = fileUpload.objectKey,
            eTag = copyResponse.copyObjectResult().eTag(),
            versionId = copyResponse.versionId(),
        )
    }
}
