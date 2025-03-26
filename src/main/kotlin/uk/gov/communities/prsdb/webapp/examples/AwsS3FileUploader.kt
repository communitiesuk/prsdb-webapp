package uk.gov.communities.prsdb.webapp.examples

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.InputStream

@Service
class AwsS3FileUploader(
    private val s3Client: S3Client,
    @Value("\${aws.s3.quarantineBucket}")
    private val bucketName: String,
) : FileUploader {
    override fun uploadFile(
        objectKey: String,
        inputStream: InputStream,
        streamSize: Long,
    ): String {
        inputStream.use { input ->
            val objectRequest =
                PutObjectRequest
                    .builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build()

            val response = s3Client.putObject(objectRequest, RequestBody.fromInputStream(input, streamSize))

            return response.toString()
        }
    }
}
