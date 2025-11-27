package uk.gov.communities.prsdb.webapp.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureReleaseModel

@Component
@ConfigurationProperties(prefix = "features")
class FeatureFlagsFromConfig {
    var featureFlags: List<FeatureFlagModel> = emptyList()
    var releases: List<FeatureReleaseModel> = emptyList()
}
