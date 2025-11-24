package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureDisabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.FeatureFlagConfig
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FLAG_WITH_RELEASE_DATE_ONE
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FLAG_WITH_RELEASE_DATE_TWO
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_GROUPED_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_GROUPED_FEATURE_FLAG_TWO
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_SINGLE_FEATURE_FLAG
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagGroupModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagModel
import uk.gov.communities.prsdb.webapp.services.interfaces.ExampleFeatureFlaggedService

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed
@PrsdbController
@RequestMapping("/$LANDLORD_PATH_SEGMENT")
class ExampleFeatureFlagTestController(
    private val exampleFeatureFlaggedService: ExampleFeatureFlaggedService,
    private val featureFlagManager: FeatureFlagManager,
) {
    @GetMapping(FEATURED_FLAGGED_SERVICE_TEST_URL_SEGMENT)
    fun featureFlaggedServiceTest(model: Model): String {
        val featureStatusText =
            if (featureFlagManager.checkFeature(EXAMPLE_SINGLE_FEATURE_FLAG)) {
                "Feature Flag in FeatureFlagConfig is ON"
            } else {
                "Feature Flag in FeatureFlagConfig is OFF"
            }
        model.addAttribute("ffTestHeading", exampleFeatureFlaggedService.getFeatureFlagPageHeading())
        model.addAttribute("ffConfigFeature", featureStatusText)

        // TODO PRSD-1683 - delete template when no longer needed
        return "featureFlagExamples/featureFlagTest"
    }

    @GetMapping(FEATURED_FLAGGED_TEMPLATE_TEST_URL_SEGMENT)
    fun featureFlaggedTemplateTest(): String = exampleFeatureFlaggedService.getTemplateName()

    @AvailableWhenFeatureEnabled(EXAMPLE_SINGLE_FEATURE_FLAG)
    @GetMapping(FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT)
    fun featureFlaggedEndpointTest(model: Model): String {
        val featureStatusText =
            if (featureFlagManager.checkFeature(EXAMPLE_SINGLE_FEATURE_FLAG)) {
                "Feature Flag in FeatureFlagConfig is ON"
            } else {
                throw IllegalStateException("Feature flag should be enabled to access this endpoint")
            }
        model.addAttribute("ffTestHeading", "Feature flagged controller endpoint - available when flag is ENABLED")
        model.addAttribute("ffConfigFeature", featureStatusText)

        return "featureFlagExamples/featureFlagTest"
    }

    @AvailableWhenFeatureDisabled(EXAMPLE_SINGLE_FEATURE_FLAG)
    @GetMapping(INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT)
    fun inverseFeatureFlaggedEndpointTest(model: Model): String {
        val featureStatusText =
            if (featureFlagManager.checkFeature(EXAMPLE_SINGLE_FEATURE_FLAG)) {
                throw IllegalStateException("Feature flag should be disabled to access this endpoint")
            } else {
                "Feature Flag in FeatureFlagConfig is OFF"
            }
        model.addAttribute("ffTestHeading", "Feature flagged controller endpoint - - available when flag is DISABLED")
        model.addAttribute("ffConfigFeature", featureStatusText)

        return "featureFlagExamples/featureFlagTest"
    }

    @AvailableWhenFeatureEnabled(EXAMPLE_GROUPED_FEATURE_FLAG_ONE)
    @GetMapping("$FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$GROUPED_FEATURES_URL_SEGMENT/$EXAMPLE_GROUPED_FEATURE_FLAG_ONE")
    fun groupedFeatureFlaggedEndpointFlagTwo(model: Model): String {
        populateModelForFeatureFlagGroupTest(model, EXAMPLE_GROUPED_FEATURE_FLAG_ONE)

        return "featureFlagExamples/featureFlagGroupTest"
    }

    @AvailableWhenFeatureDisabled(EXAMPLE_GROUPED_FEATURE_FLAG_ONE)
    @GetMapping("$INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$GROUPED_FEATURES_URL_SEGMENT/$EXAMPLE_GROUPED_FEATURE_FLAG_ONE")
    fun groupedFeatureInverseFlaggedEndpointFlagTwo(model: Model): String {
        populateModelForFeatureFlagGroupTest(model, EXAMPLE_GROUPED_FEATURE_FLAG_ONE)

        return "featureFlagExamples/featureFlagGroupTest"
    }

    @AvailableWhenFeatureEnabled(EXAMPLE_GROUPED_FEATURE_FLAG_TWO)
    @GetMapping("$FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$GROUPED_FEATURES_URL_SEGMENT/$EXAMPLE_GROUPED_FEATURE_FLAG_TWO")
    fun groupedFeatureFlaggedEndpointFlagThree(model: Model): String {
        populateModelForFeatureFlagGroupTest(model, EXAMPLE_GROUPED_FEATURE_FLAG_TWO)

        return "featureFlagExamples/featureFlagGroupTest"
    }

    @AvailableWhenFeatureDisabled(EXAMPLE_GROUPED_FEATURE_FLAG_TWO)
    @GetMapping("$INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$GROUPED_FEATURES_URL_SEGMENT/$EXAMPLE_GROUPED_FEATURE_FLAG_TWO")
    fun groupedFeatureInverseFlaggedEndpointFlagThree(model: Model): String {
        populateModelForFeatureFlagGroupTest(model, EXAMPLE_GROUPED_FEATURE_FLAG_TWO)

        return "featureFlagExamples/featureFlagGroupTest"
    }

    @AvailableWhenFeatureEnabled(EXAMPLE_FLAG_WITH_RELEASE_DATE_ONE)
    @GetMapping("$FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$GROUPED_FEATURES_URL_SEGMENT/$EXAMPLE_FLAG_WITH_RELEASE_DATE_ONE")
    fun flagWithReleaseDateEnabled(model: Model): String {
        populateModelForFeatureFlagReleaseDateTest(model, EXAMPLE_FLAG_WITH_RELEASE_DATE_ONE)

        return "featureFlagExamples/featureFlagReleaseDateTest"
    }

    @AvailableWhenFeatureDisabled(EXAMPLE_FLAG_WITH_RELEASE_DATE_TWO)
    @GetMapping("$INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$GROUPED_FEATURES_URL_SEGMENT/$EXAMPLE_FLAG_WITH_RELEASE_DATE_TWO")
    fun flagWithReleaseDateDisabled(model: Model): String {
        populateModelForFeatureFlagReleaseDateTest(model, EXAMPLE_FLAG_WITH_RELEASE_DATE_TWO)
        model.addAttribute("ffTestHeading", "Inverse feature flagged controller endpoint - available when flag is DISABLED")

        return "featureFlagExamples/featureFlagReleaseDateTest"
    }

    private fun getFeatureFlagModelFromConfig(featureName: String): FeatureFlagModel =
        FeatureFlagConfig.featureFlags.firstOrNull { it.name == featureName }
            ?: throw IllegalArgumentException("Feature flag $featureName not found in config")

    private fun getFeatureFlagGroupModelFromConfig(groupName: String): FeatureFlagGroupModel =
        FeatureFlagConfig.featureGroups.firstOrNull { it.name == groupName }
            ?: throw IllegalArgumentException("Feature flag group $groupName not found in config")

    private fun populateModelForFeatureFlagGroupTest(
        model: Model,
        flagName: String,
    ): Pair<FeatureFlagModel, FeatureFlagGroupModel> {
        val featureFlagSetInConfig = getFeatureFlagModelFromConfig(flagName)
        if (featureFlagSetInConfig.flagGroup == null) {
            throw IllegalArgumentException("Feature flag $flagName is not part of a feature flag group")
        }
        val featureFlagGroupSetInConfig = getFeatureFlagGroupModelFromConfig(featureFlagSetInConfig.flagGroup)
        val featureState =
            if (featureFlagManager.checkFeature(flagName)) {
                "ENABLED"
            } else {
                "DISABLED"
            }

        model.addAttribute("ffTestHeading", "Feature flagged controller endpoint - available when flag is ENABLED")
        model.addAttribute("flagGroupName", featureFlagSetInConfig.flagGroup)
        model.addAttribute("ffSubHeading", "Configuration for $flagName")

        model.addAttribute(
            "flagValueInConfig",
            "Flag $flagName was initialised to ${featureFlagSetInConfig.enabled.toString().uppercase()} in FeatureFlagConfig",
        )
        model.addAttribute(
            "flagGroupValueInConfig",
            "Flag Group ${featureFlagSetInConfig.flagGroup} was initialised to " +
                "${featureFlagGroupSetInConfig.enabled.toString().uppercase()} in FeatureFlagConfig",
        )
        model.addAttribute("featureEnabled", "This feature is $featureState")

        return Pair(featureFlagSetInConfig, featureFlagGroupSetInConfig)
    }

    private fun populateModelForFeatureFlagReleaseDateTest(
        model: Model,
        flagName: String,
    ) {
        val (featureFlagConfig, featureFlagGroupConfig) = populateModelForFeatureFlagGroupTest(model, flagName)
        model.addAttribute(
            "flagGroupReleaseDateInConfig",
            "Flag Group ${featureFlagConfig.flagGroup} has a release date of ${featureFlagGroupConfig.releaseDate}",
        )
    }

    companion object {
        const val FEATURED_FLAGGED_SERVICE_TEST_URL_SEGMENT = "feature-flagged-service-test"
        const val FEATURED_FLAGGED_TEMPLATE_TEST_URL_SEGMENT = "feature-flagged-template-test"
        const val FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT = "feature-flagged-endpoint-test"
        const val INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT = "inverse-feature-flagged-endpoint-test"
        const val GROUPED_FEATURES_URL_SEGMENT = "grouped-features"

        const val FEATURED_FLAGGED_SERVICE_TEST_URL_ROUTE = "/$LANDLORD_PATH_SEGMENT/$FEATURED_FLAGGED_SERVICE_TEST_URL_SEGMENT"
        const val FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE = "/$LANDLORD_PATH_SEGMENT/$FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT"
        const val INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT"
        const val FEATURED_FLAGGED_TEMPLATE_TEST_URL_ROUTE = "/$LANDLORD_PATH_SEGMENT/$FEATURED_FLAGGED_TEMPLATE_TEST_URL_SEGMENT"
        const val FEATURE_FLAGGED_GROUPED_ENDPOINT_GROUPED_FLAG_ONE_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$GROUPED_FEATURES_URL_SEGMENT" +
                "/$EXAMPLE_GROUPED_FEATURE_FLAG_ONE"
        const val FEATURE_FLAGGED_GROUPED_ENDPOINT_GROUPED_FLAG_TWO_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$GROUPED_FEATURES_URL_SEGMENT" +
                "/$EXAMPLE_GROUPED_FEATURE_FLAG_TWO"
        const val FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$GROUPED_FEATURES_URL_SEGMENT" +
                "/$EXAMPLE_FLAG_WITH_RELEASE_DATE_ONE"
    }
}
