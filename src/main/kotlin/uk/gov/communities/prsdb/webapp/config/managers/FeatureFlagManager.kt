package uk.gov.communities.prsdb.webapp.config.managers

import org.ff4j.FF4j
import org.ff4j.core.Feature
import org.ff4j.exception.GroupNotFoundException
import org.ff4j.property.PropertyDate
import uk.gov.communities.prsdb.webapp.config.FeatureFlipStrategyInitialiser
import uk.gov.communities.prsdb.webapp.config.factories.BooleanFlippingStrategyFactory
import uk.gov.communities.prsdb.webapp.config.factories.ReleaseDateFlippingStrategyFactory
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagConfigModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureReleaseConfigModel

class FeatureFlagManager() : FF4j() {
    private val featureFlipStrategyInitialiser =
        FeatureFlipStrategyInitialiser(
            booleanFlipStrategyFactory = BooleanFlippingStrategyFactory(),
            releaseDateFlipStrategyFactory = ReleaseDateFlippingStrategyFactory(),
        )

    fun initializeFeatureFlags(featureFlags: List<FeatureFlagConfigModel>) {
        featureFlags.forEach { flag ->
            initializeFeatureFlag(flag)
        }
    }

    private fun initializeFeatureFlag(flag: FeatureFlagConfigModel) {
        val feature = Feature(flag.name, flag.enabled)
        feature.addProperty(PropertyDate("expiryDate", DateTimeHelper.getJavaDateFromLocalDate(flag.expiryDate)))
        feature.group = flag.release
        feature.flippingStrategy =
            flag.strategyConfig?.let {
                featureFlipStrategyInitialiser.getFlipStrategyOrNull(it)
            }

        this.createFeature(feature)
    }

    fun initialiseFeatureReleases(featureReleases: List<FeatureReleaseConfigModel>) {
        try {
            featureReleases.forEach { group ->
                setGroupEnabledState(group)
                if (group.strategyConfig != null) {
                    // This will override any flip strategies set at the feature level within this group
                    setFlipStrategyForFeaturesInGroup(group)
                }
            }
        } catch (e: GroupNotFoundException) {
            throw (
                RuntimeException(
                    e.message +
                        ". Check that at least one feature in FeatureFlagConfig.featureFlags has this group's name set as flagGroup.",
                )
            )
        }
    }

    private fun setGroupEnabledState(group: FeatureReleaseConfigModel) {
        if (group.enabled) {
            this.enableFeatureRelease(group.name)
        } else {
            this.disableFeatureRelease(group.name)
        }
    }

    private fun setFlipStrategyForFeaturesInGroup(group: FeatureReleaseConfigModel) {
        this.getFeaturesByGroup(group.name).forEach { (_, feature) ->
            feature.flippingStrategy =
                group.strategyConfig?.let {
                    featureFlipStrategyInitialiser.getFlipStrategyOrNull(it)
                }
        }
    }

    fun checkFeature(featureName: String): Boolean = super.check(featureName)

    fun enableFeature(flagName: String) = super.enable(flagName)

    fun disableFeature(flagName: String) = super.disable(flagName)

    fun enableFeatureRelease(groupName: String) = super.enableGroup(groupName)

    fun disableFeatureRelease(groupName: String) = super.disableGroup(groupName)
}
