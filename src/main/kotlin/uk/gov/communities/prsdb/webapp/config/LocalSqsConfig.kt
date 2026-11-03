package uk.gov.communities.prsdb.webapp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import java.net.URI

@Configuration
@Profile("local")
class LocalSqsConfig {
    @Bean
    fun sqsAsyncClient(
        @Value("\${local.sqs.endpoint}") endpoint: String,
    ): SqsAsyncClient =
        SqsAsyncClient
            .builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.EU_WEST_2)
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("local", "local")))
            .build()
}
