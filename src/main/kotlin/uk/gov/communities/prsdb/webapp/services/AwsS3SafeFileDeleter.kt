package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import software.amazon.awssdk.services.s3.S3Client
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload

@PrsdbWebService
class AwsS3SafeFileDeleter(
    private val s3Client: S3Client,
    @Value("\${aws.s3.safeBucket}")
    val safeBucketName: String,
) : SafeFileDeleter {
    override fun deleteFile(fileUpload: FileUpload): Boolean {
        val deleteResponse =
            s3Client
                .deleteObject { request ->
                    request.bucket(safeBucketName).key(fileUpload.objectKey)
                }

        return deleteResponse.sdkHttpResponse().isSuccessful
    }
}
