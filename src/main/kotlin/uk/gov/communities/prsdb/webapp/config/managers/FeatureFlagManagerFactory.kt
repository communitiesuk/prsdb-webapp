package uk.gov.communities.prsdb.webapp.config.managers

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.config.FeatureFlagsFromConfig

@ComponentScan(basePackages = ["uk.gov.communities.prsdb.webapp"])
@Configuration
class FeatureFlagManagerFactory(
    private val featureFlagsFromConfig: FeatureFlagsFromConfig,
) {
    @Bean
    fun featureFlagManager(): FeatureFlagManager {
        val featureFlagManager = FeatureFlagManager()
        featureFlagManager.initializeFeatureFlags(featureFlagsFromConfig.featureFlags)
        featureFlagManager.initialiseFeatureReleases(featureFlagsFromConfig.releases)
        return featureFlagManager
    }
}
