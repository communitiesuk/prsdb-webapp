package uk.gov.communities.prsdb.webapp.config

import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import uk.gov.communities.prsdb.webapp.constants.featureFlagNames
import uk.gov.communities.prsdb.webapp.constants.featureFlagReleaseNames
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureReleaseModel

@Component
@ConfigurationProperties(prefix = "features")
class FeatureFlagsFromConfig(
    private val featureFlagNamesList: List<String> = featureFlagNames,
    private val releaseNamesList: List<String> = featureFlagReleaseNames,
) : InitializingBean {
    var featureFlags: List<FeatureFlagModel> = emptyList()
    var releases: List<FeatureReleaseModel> = emptyList()

    override fun afterPropertiesSet() {
        featureFlags.forEach { feature ->
            if (feature.name !in featureFlagNamesList) {
                throw IllegalStateException(
                    "Feature flag name ${feature.name} must be added as a const val and included in featureFlagNames",
                )
            }
        }

        releases.forEach { release ->
            if (release.name !in releaseNamesList) {
                throw IllegalStateException(
                    "Feature release name ${release.name} must be added as a const val and included in featureFlagReleaseNames",
                )
            }
        }
    }
}
