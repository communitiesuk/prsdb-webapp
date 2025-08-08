package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload

@Service
class AwsS3QuarantinedFileDeleter(
    private val s3Client: S3Client,
    @Value("\${aws.s3.quarantineBucket}")
    val quarantineBucketName: String,
) : QuarantinedFileDeleter {
    override fun deleteFile(fileUpload: FileUpload): Boolean {
        val deleteResponse =
            s3Client
                .deleteObject { request ->
                    request.bucket(quarantineBucketName).key(fileUpload.objectKey)
                }

        return deleteResponse.sdkHttpResponse().isSuccessful
    }
}
