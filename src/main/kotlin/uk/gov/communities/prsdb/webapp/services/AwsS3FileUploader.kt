package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.transfer.s3.S3TransferManager
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import java.io.InputStream

@PrsdbWebService
class AwsS3FileUploader(
    private val transferManager: S3TransferManager,
    @Value("\${aws.s3.quarantineBucket}")
    private val bucketName: String,
) : FileUploader {
    override fun uploadFile(
        objectKey: String,
        inputStream: InputStream,
    ): Boolean {
        inputStream.use { input ->
            val requestBody = AsyncRequestBody.forBlockingInputStream(null)
            val upload =
                transferManager.upload { builder ->
                    builder
                        .requestBody(requestBody)
                        .putObjectRequest { request -> request.bucket(bucketName).key(objectKey) }
                        .build()
                }

            requestBody.writeInputStream(input)

            val response = upload.completionFuture().join().response()

            return response.sdkHttpResponse().isSuccessful
        }
    }
}
