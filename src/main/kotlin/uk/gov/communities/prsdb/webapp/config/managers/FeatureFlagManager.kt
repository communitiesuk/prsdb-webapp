package uk.gov.communities.prsdb.webapp.config.managers

import org.ff4j.FF4j
import org.ff4j.core.Feature
import org.ff4j.property.PropertyDate
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagGroupModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagModel

class FeatureFlagManager : FF4j() {
    fun initializeFeatureFlags(featureFlags: List<FeatureFlagModel>) {
        featureFlags.forEach { flag ->
            initializeFeatureFlag(flag)
        }
    }

    private fun initializeFeatureFlag(flag: FeatureFlagModel) {
        val feature = Feature(flag.name, flag.enabled)
        feature.addProperty(PropertyDate("expiryDate", DateTimeHelper.getJavaDateFromLocalDate(flag.expiryDate)))
        feature.group = flag.flagGroup

        this.createFeature(feature)
    }

    fun initialiseFeatureFlagGroups(featureFlagGroups: List<FeatureFlagGroupModel>) {
        featureFlagGroups.forEach { group ->
            if (group.enabled) {
                this.enableFeatureGroup(group.name)
            } else {
                this.disableFeatureGroup(group.name)
            }
        }
    }

    fun checkFeature(featureName: String): Boolean = super.check(featureName)

    fun enableFeature(flagName: String) = super.enable(flagName)

    fun disableFeature(flagName: String) = super.disable(flagName)

    fun enableFeatureGroup(groupName: String) = super.enableGroup(groupName)

    fun disableFeatureGroup(groupName: String) = super.disableGroup(groupName)
}
