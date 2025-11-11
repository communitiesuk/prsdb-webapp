package uk.gov.communities.prsdb.webapp.config.managers

import org.ff4j.FF4j
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagModel

class FeatureFlagManager : FF4j() {
    fun initializeFeatureFlags(featureFlags: List<FeatureFlagModel>) {
        featureFlags.forEach { flag ->
            this.createFeature(flag.name, flag.enabled)
        }
    }
}
