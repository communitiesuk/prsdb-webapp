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

        missingFlagOrReleaseMessage = addFeaturesMissingFromFeatureFlagNamesList(missingFlagOrReleaseMessage)
        missingFlagOrReleaseMessage = addReleasesMissingFromReleaseNamesList(missingFlagOrReleaseMessage)
        missingFlagOrReleaseMessage = addFeaturesMissingFromYamlConfig(missingFlagOrReleaseMessage)
        missingFlagOrReleaseMessage = addReleasesMissingFromYamlConfig(missingFlagOrReleaseMessage)

        if (missingFlagOrReleaseMessage.isNotEmpty()) {
            throw IllegalStateException(missingFlagOrReleaseMessage.trim())
        }
    }

    private fun addFeaturesMissingFromFeatureFlagNamesList(errorMessage: String): String {
        var missingFlagOrReleaseMessage = errorMessage
        featureFlags.forEach { feature ->
            if (feature.name !in featureFlagNamesList) {
                missingFlagOrReleaseMessage +=
                    "Feature flag name ${feature.name} must be added as a const val and included in featureFlagNames \n"
            }
        }
        return missingFlagOrReleaseMessage
    }

    private fun addReleasesMissingFromReleaseNamesList(errorMessage: String): String {
        var missingFlagOrReleaseMessage = errorMessage
        releases.forEach { release ->
            if (release.name !in releaseNamesList) {
                missingFlagOrReleaseMessage +=
                    "Feature release name ${release.name} must be added as a const val and included in featureFlagReleaseNames \n"
            }
        }
        return missingFlagOrReleaseMessage
    }

    private fun addFeaturesMissingFromYamlConfig(errorMessage: String): String {
        var missingFlagOrReleaseMessage = errorMessage
        featureFlagNamesList.forEach { featureName ->
            if (featureFlags.none { it.name == featureName }) {
                missingFlagOrReleaseMessage +=
                    "Feature flag name $featureName must be included in the features.featureFlags list in the YAML config \n"
            }
        }
        return missingFlagOrReleaseMessage
    }

    private fun addReleasesMissingFromYamlConfig(errorMessage: String): String {
        var missingFlagOrReleaseMessage = errorMessage
        releaseNamesList.forEach { releaseName ->
            if (releases.none { it.name == releaseName }) {
                missingFlagOrReleaseMessage +=
                    "Feature release name $releaseName must be included in the features.releases list in the YAML config \n"
            }
        }
        return missingFlagOrReleaseMessage
    }
}
