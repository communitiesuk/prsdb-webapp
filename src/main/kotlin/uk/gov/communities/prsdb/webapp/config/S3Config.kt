package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.transfer.s3.S3TransferManager

@Configuration
class S3Config {
    @Bean
    fun s3Client(): S3Client = S3Client.builder().build()

    @Bean
    fun s3(): S3TransferManager {
        val client = S3AsyncClient.crtBuilder().build()
        return S3TransferManager.builder().s3Client(client).build()
    }
}
