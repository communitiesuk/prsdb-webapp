package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.transfer.s3.S3TransferManager

@Service
class AwsS3FileDequarantiner(
    private val transferManager: S3TransferManager,
    private val s3Client: S3AsyncClient,
) : FileDequarantiner {
    @Value("\${aws.s3.quarantineBucket}")
    lateinit var quarantineBucketName: String

    @Value("\${aws.s3.safeBucket}")
    lateinit var safeBucketName: String

    override fun dequarantine(objectKey: String): Boolean {
        val copyResponse =
            transferManager
                .copy { builder ->
                    builder.copyObjectRequest { requestBuilder ->
                        requestBuilder
                            .sourceBucket(quarantineBucketName)
                            .sourceKey(objectKey)
                            .destinationBucket(safeBucketName)
                            .destinationKey(objectKey)
                    }
                }.completionFuture()
                .join()
                .response()

        if (!copyResponse.sdkHttpResponse().isSuccessful) {
            return false
        }

        val deleteResponse =
            s3Client
                .deleteObject { request ->
                    request.bucket(quarantineBucketName).key(objectKey)
                }.join()

        return deleteResponse.sdkHttpResponse().isSuccessful
    }

    override fun isFileDequarantined(objectKey: String): Boolean {
        val response =
            s3Client
                .headObject { request ->
                    request
                        .bucket("prsdb-quarantine-integration")
                        .key(objectKey)
                }.toCompletableFuture()
                .join()
                .sdkHttpResponse()

        return if (response?.isSuccessful == true) {
            true
        } else if (response?.statusCode() == 404) {
            false
        } else {
            throw IllegalStateException("Unexpected response from S3: $response")
        }
    }
}
