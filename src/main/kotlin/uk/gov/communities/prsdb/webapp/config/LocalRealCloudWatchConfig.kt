package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.clients.AwsCloudWatchMetricsClient
import uk.gov.communities.prsdb.webapp.clients.CloudWatchMetricsClient

// THROWAWAY LOCAL-ONLY CONFIG — DO NOT COMMIT.
// Loads only under the `local & real-cloudwatch` profile so a normal local run (which uses
// StubCloudWatchMetricsClient) and every deployed profile are completely unaffected.
@Profile("local & real-cloudwatch")
@PrsdbWebConfiguration("localRealCloudWatchConfig")
class LocalRealCloudWatchConfig {
    @Bean
    @Primary
    fun realCloudWatchMetricsClient(): CloudWatchMetricsClient {
        val credentialsProvider = DefaultCredentialsProvider.create()

        val euWest2Client =
            CloudWatchClient
                .builder()
                .region(Region.EU_WEST_2)
                .credentialsProvider(credentialsProvider)
                .build()

        val usEast1Client =
            CloudWatchClient
                .builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(credentialsProvider)
                .build()

        return AwsCloudWatchMetricsClient(euWest2Client, usEast1Client)
    }
}
