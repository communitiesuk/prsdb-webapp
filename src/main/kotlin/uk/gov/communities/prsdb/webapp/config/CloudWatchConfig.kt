package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration

@Profile("!local")
@PrsdbWebConfiguration
class CloudWatchConfig {
    @Bean
    @Primary
    fun cloudWatchClient(
        regionProvider: AwsRegionProvider,
        credentialsProvider: AwsCredentialsProvider,
    ): CloudWatchClient =
        CloudWatchClient
            .builder()
            .region(regionProvider.region)
            .credentialsProvider(credentialsProvider)
            .build()

    @Bean("cloudFrontCloudWatchClient")
    fun cloudFrontCloudWatchClient(credentialsProvider: AwsCredentialsProvider): CloudWatchClient =
        CloudWatchClient
            .builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(credentialsProvider)
            .build()
}
