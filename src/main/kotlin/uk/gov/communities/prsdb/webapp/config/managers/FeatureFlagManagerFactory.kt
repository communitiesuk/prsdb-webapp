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
        // If a flag is in a flag group, the flag group's enabled value overrides the individual flag's enabled value
        featureFlagManager.initialiseFeatureReleases(featureFlagsFromConfig.releases)
        return featureFlagManager
    }
}
