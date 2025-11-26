package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager

// TODO: PRSD-1647 - consider renaming this, it's more creating the manager now than defining feature config
@ComponentScan(basePackages = ["uk.gov.communities.prsdb.webapp"])
@Configuration
class FeatureFlagConfig(
    private val featureFlagsFromConfig: FeatureFlagsFromApplicationConfig,
) {
    @Bean
    fun featureFlagManager(): FeatureFlagManager {
        val featureFlagManager = FeatureFlagManager()
        featureFlagManager.initializeFeatureFlags(featureFlagsFromConfig.featureFlags)
        // If a flag is in a flag group, the flag group's enabled value overrides the individual flag's enabled value
        featureFlagManager.initialiseFeatureFlagGroups(featureFlagsFromConfig.featureGroups)
        return featureFlagManager
    }
}
