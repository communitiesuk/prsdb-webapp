package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.costexplorer.CostExplorerClient
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration

@Profile("!local")
@PrsdbWebConfiguration("prsdbCostExplorerConfig")
class CostExplorerConfig {
    @Bean
    fun costExplorerClient(credentialsProvider: AwsCredentialsProvider): CostExplorerClient =
        CostExplorerClient
            .builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(credentialsProvider)
            .build()
}
