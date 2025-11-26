package uk.gov.communities.prsdb.webapp.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagGroupModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagModel

@Component
@ConfigurationProperties
class FeatureFlagsFromApplicationConfig {
    var featureFlags: List<FeatureFlagModel> = emptyList()
    var featureGroups: List<FeatureFlagGroupModel> = emptyList()
}
