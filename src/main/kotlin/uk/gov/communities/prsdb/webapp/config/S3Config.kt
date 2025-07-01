package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.transfer.s3.S3TransferManager

@Configuration
class S3Config {
    @Bean
    fun s3Client(): S3AsyncClient = S3AsyncClient.crtBuilder().build()

    @Bean
    fun s3(client: S3AsyncClient): S3TransferManager = S3TransferManager.builder().s3Client(client).build()
}
