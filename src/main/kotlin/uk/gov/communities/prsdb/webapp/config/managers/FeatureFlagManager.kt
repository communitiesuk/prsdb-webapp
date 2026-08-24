package uk.gov.communities.prsdb.webapp.config.managers

import org.ff4j.FF4j
import org.ff4j.core.Feature
import org.ff4j.core.FlippingExecutionContext
import org.ff4j.exception.GroupNotFoundException
import org.ff4j.property.PropertyDate
import org.springframework.stereotype.Component
import uk.gov.communities.prsdb.webapp.config.FeatureFlipStrategyInitialiser
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagConfigModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagOverrides
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureReleaseConfigModel
import uk.gov.communities.prsdb.webapp.services.FeatureFlagOverrideService

@Component
class FeatureFlagManager(
    private val featureFlipStrategyInitialiser: FeatureFlipStrategyInitialiser,
    private val featureFlagOverrideService: FeatureFlagOverrideService? = null,
) : FF4j() {
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
            featureReleases.forEach { release ->
                setReleaseEnabledState(release)
                if (release.strategyConfig != null) {
                    // This will override any flip strategies set at the feature level within this release
                    setFlipStrategyForFeaturesInRelease(release)
                }
            }
        } catch (e: GroupNotFoundException) {
            throw (
                RuntimeException(
                    e.message +
                        ". Check that at least one feature in the yaml config has this release's name set as release.",
                )
            )
        }
    }

    private fun setReleaseEnabledState(group: FeatureReleaseConfigModel) {
        if (group.enabled) {
            this.enableFeatureRelease(group.name)
        } else {
            this.disableFeatureRelease(group.name)
        }
    }

    private fun setFlipStrategyForFeaturesInRelease(group: FeatureReleaseConfigModel) {
        this.getFeaturesByGroup(group.name).forEach { (_, feature) ->
            feature.flippingStrategy =
                group.strategyConfig?.let {
                    featureFlipStrategyInitialiser.getFlipStrategyOrNull(it)
                }
        }
    }

    fun checkFeature(featureName: String): Boolean = super.check(featureName)

    override fun check(
        featureName: String,
        executionContext: FlippingExecutionContext?,
    ): Boolean {
        val overrides = featureFlagOverrideService?.getOverrides() ?: FeatureFlagOverrides()
        if (overrides.isEmpty()) return super.check(featureName, executionContext)

        val releaseName = getFeature(featureName).group
        if (!releaseName.isNullOrEmpty()) {
            overrides.releases[releaseName]?.let { return it }
        }
        overrides.flags[featureName]?.let { return it }

        return super.check(featureName, executionContext)
    }

    fun enableFeature(flagName: String) = super.enable(flagName)

    fun disableFeature(flagName: String) = super.disable(flagName)

    fun enableFeatureRelease(groupName: String) = super.enableGroup(groupName)

    fun disableFeatureRelease(groupName: String) = super.disableGroup(groupName)
}
