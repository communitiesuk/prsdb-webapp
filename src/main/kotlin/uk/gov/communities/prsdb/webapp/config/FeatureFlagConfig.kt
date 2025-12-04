package uk.gov.communities.prsdb.webapp.config

import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.featureFlagNames
import uk.gov.communities.prsdb.webapp.constants.featureFlagReleaseNames
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagConfigModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureReleaseConfigModel

@Configuration
@ComponentScan(basePackages = ["uk.gov.communities.prsdb.webapp"])
@ConfigurationProperties(prefix = "features")
class FeatureFlagConfig(
    private val featureFlagNamesList: List<String> = featureFlagNames,
    private val releaseNamesList: List<String> = featureFlagReleaseNames,
) : InitializingBean {
    var featureFlags: List<FeatureFlagConfigModel> = emptyList()
    var releases: List<FeatureReleaseConfigModel> = emptyList()

    @Bean
    fun featureFlagManager(strategyInitialiser: FeatureFlipStrategyInitialiser): FeatureFlagManager {
        val featureFlagManager = FeatureFlagManager(strategyInitialiser)
        featureFlagManager.initializeFeatureFlags(featureFlags)
        featureFlagManager.initialiseFeatureReleases(releases)
        return featureFlagManager
    }

    override fun afterPropertiesSet() {
        var missingFlagOrReleaseMessage = ""

        featureFlags.forEach { feature ->
            if (feature.name !in featureFlagNamesList) {
                missingFlagOrReleaseMessage +=
                    "Feature flag name ${feature.name} must be added as a const val and included in featureFlagNames \n"
            }
        }

        releases.forEach { release ->
            if (release.name !in releaseNamesList) {
                missingFlagOrReleaseMessage +=
                    "Feature release name ${release.name} must be added as a const val and included in featureFlagReleaseNames \n"
            }
        }

        if (missingFlagOrReleaseMessage.isNotEmpty()) {
            throw IllegalStateException(missingFlagOrReleaseMessage.trim())
        }
    }
}
