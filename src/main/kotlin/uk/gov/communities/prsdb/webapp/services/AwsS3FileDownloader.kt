package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import java.time.Duration

@PrsdbWebService
class AwsS3FileDownloader(
    @Value("\${aws.s3.safeBucket}")
    val safeBucketName: String,
) : FileDownloader {
    override fun getDownloadUrl(
        fileUpload: FileUpload,
        fileName: String?,
    ): String {
        if (!isFileDownloadable(fileUpload)) {
            throw PrsdbWebException(
                "File with object key ${fileUpload.objectKey} is not downloadable. " +
                    "Status: ${fileUpload.status}",
            )
        }

        val preSigner = S3Presigner.create()

        val objectRequestBuilder =
            GetObjectRequest
                .builder()
                .bucket(safeBucketName)
                .key(fileUpload.objectKey)
                .versionId(fileUpload.versionId)

        if (fileName != null) {
            objectRequestBuilder.responseContentDisposition("attachment; filename=\"$fileName\"")
        }

        val presignRequest =
            GetObjectPresignRequest
                .builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(objectRequestBuilder.build())
                .build()

        val presignedRequest = preSigner.presignGetObject(presignRequest)

        return presignedRequest.url().toExternalForm()
    }
}
