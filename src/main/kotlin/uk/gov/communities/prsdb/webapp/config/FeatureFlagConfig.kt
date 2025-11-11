package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE

@Configuration
class FeatureFlagConfig {
    @Bean
    fun featureFlagManager(): FeatureFlagManager {
        val featureFlagManager = FeatureFlagManager()

        featureFlagManager.createFeature(EXAMPLE_FEATURE_FLAG_ONE, true)

        return featureFlagManager
    }
}
