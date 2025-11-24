package uk.gov.communities.prsdb.webapp.config.managers

import org.ff4j.FF4j
import org.ff4j.core.Feature
import org.ff4j.core.FlippingStrategy
import org.ff4j.exception.GroupNotFoundException
import org.ff4j.property.PropertyDate
import org.ff4j.strategy.time.ReleaseDateFlipStrategy
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
            enableOrDisableAllFeaturesInGroup(group)
            if (group.releaseDate != null) {
                addReleaseDateFlippingStrategyToFeaturesInGroup(group.name, group.releaseDate)
            }
        }
    }

    private fun enableOrDisableAllFeaturesInGroup(featureFlagGroup: FeatureFlagGroupModel) {
        try {
            if (featureFlagGroup.enabled) {
                this.enableFeatureGroup(featureFlagGroup.name)
            } else {
                this.disableFeatureGroup(featureFlagGroup.name)
            }
        } catch (e: GroupNotFoundException) {
            throw (
                RuntimeException(
                    e.message +
                        ". Check that at least one feature in FeatureFlagConfig.featureFlags has " +
                        "${featureFlagGroup.name} set as flagGroup.",
                )
            )
        }
    }

    private fun addReleaseDateFlippingStrategyToFeaturesInGroup(
        groupName: String,
        releaseDate: java.time.LocalDate,
    ) {
        val featuresInGroup = this.getFeaturesByGroup(groupName)
        val flippingStrategy: FlippingStrategy = ReleaseDateFlipStrategy(DateTimeHelper.getJavaDateFromLocalDate(releaseDate))
        featuresInGroup.forEach { (_, feature) ->
            feature.flippingStrategy = flippingStrategy
        }
    }

    fun checkFeature(featureName: String): Boolean = super.check(featureName)

    fun enableFeature(flagName: String) = super.enable(flagName)

    fun disableFeature(flagName: String) = super.disable(flagName)

    fun enableFeatureGroup(groupName: String) = super.enableGroup(groupName)

    fun disableFeatureGroup(groupName: String) = super.disableGroup(groupName)
}
