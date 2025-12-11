package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureDisabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.FeatureFlagConfig
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_FOUR
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_THREE
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_TWO
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagConfigModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureReleaseConfigModel
import uk.gov.communities.prsdb.webapp.services.interfaces.ExampleFeatureFlaggedService

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed
@PrsdbController
@RequestMapping("/$LANDLORD_PATH_SEGMENT")
class ExampleFeatureFlagTestController(
    private val exampleFeatureFlaggedService: ExampleFeatureFlaggedService,
    private val featureFlagManager: FeatureFlagManager,
    private val featureFlagConfig: FeatureFlagConfig,
) {
    @GetMapping(FEATURED_FLAGGED_SERVICE_TEST_URL_SEGMENT)
    fun featureFlaggedServiceTest(model: Model): String {
        val featureStatusText =
            if (featureFlagManager.checkFeature(EXAMPLE_FEATURE_FLAG_ONE)) {
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

    @AvailableWhenFeatureEnabled(EXAMPLE_FEATURE_FLAG_ONE)
    @GetMapping(FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT)
    fun featureFlaggedEndpointTest(model: Model): String {
        val featureStatusText =
            if (featureFlagManager.checkFeature(EXAMPLE_FEATURE_FLAG_ONE)) {
                "Feature Flag in FeatureFlagConfig is ON"
            } else {
                throw IllegalStateException("Feature flag should be enabled to access this endpoint")
            }
        model.addAttribute("ffTestHeading", "Feature flagged controller endpoint - available when flag is ENABLED")
        model.addAttribute("ffConfigFeature", featureStatusText)

        return "featureFlagExamples/featureFlagTest"
    }

    @AvailableWhenFeatureDisabled(EXAMPLE_FEATURE_FLAG_ONE)
    @GetMapping(INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT)
    fun inverseFeatureFlaggedEndpointTest(model: Model): String {
        val featureStatusText =
            if (featureFlagManager.checkFeature(EXAMPLE_FEATURE_FLAG_ONE)) {
                throw IllegalStateException("Feature flag should be disabled to access this endpoint")
            } else {
                "Feature Flag in FeatureFlagConfig is OFF"
            }
        model.addAttribute("ffTestHeading", "Feature flagged controller endpoint - - available when flag is DISABLED")
        model.addAttribute("ffConfigFeature", featureStatusText)

        return "featureFlagExamples/featureFlagTest"
    }

    @AvailableWhenFeatureEnabled(EXAMPLE_FEATURE_FLAG_TWO)
    @GetMapping("$FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$FEATURE_RELEASE_URL_SEGMENT/$EXAMPLE_FEATURE_FLAG_TWO")
    fun featureReleaseFlaggedEndpointFlagTwo(model: Model): String {
        populateModelForFeatureReleaseTest(model, EXAMPLE_FEATURE_FLAG_TWO)

        return "featureFlagExamples/featureReleaseTest"
    }

    @AvailableWhenFeatureDisabled(EXAMPLE_FEATURE_FLAG_TWO)
    @GetMapping("$INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$FEATURE_RELEASE_URL_SEGMENT/$EXAMPLE_FEATURE_FLAG_TWO")
    fun featureReleaseInverseFlaggedEndpointFlagTwo(model: Model): String {
        populateModelForFeatureReleaseTest(model, EXAMPLE_FEATURE_FLAG_TWO, inverseFlaggedEndpoint = true)

        return "featureFlagExamples/featureReleaseTest"
    }

    @AvailableWhenFeatureEnabled(EXAMPLE_FEATURE_FLAG_THREE)
    @GetMapping("$FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$FEATURE_RELEASE_URL_SEGMENT/$EXAMPLE_FEATURE_FLAG_THREE")
    fun featureReleaseFlaggedEndpointFlagThree(model: Model): String {
        populateModelForFeatureReleaseTest(model, EXAMPLE_FEATURE_FLAG_THREE)

        return "featureFlagExamples/featureReleaseTest"
    }

    @AvailableWhenFeatureDisabled(EXAMPLE_FEATURE_FLAG_THREE)
    @GetMapping("$INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$FEATURE_RELEASE_URL_SEGMENT/$EXAMPLE_FEATURE_FLAG_THREE")
    fun featureReleaseInverseFlaggedEndpointFlagThree(model: Model): String {
        populateModelForFeatureReleaseTest(model, EXAMPLE_FEATURE_FLAG_THREE, inverseFlaggedEndpoint = true)

        return "featureFlagExamples/featureReleaseTest"
    }

    @AvailableWhenFeatureEnabled(EXAMPLE_FEATURE_FLAG_FOUR)
    @GetMapping("$FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$FEATURE_RELEASE_URL_SEGMENT/$EXAMPLE_FEATURE_FLAG_FOUR")
    fun featureReleaseStrategyFlaggedEndpointFlagFour(model: Model): String {
        populateModelForFeatureReleaseTest(model, EXAMPLE_FEATURE_FLAG_FOUR)

        return "featureFlagExamples/releaseWithReleaseStrategyTest"
    }

    @AvailableWhenFeatureDisabled(EXAMPLE_FEATURE_FLAG_FOUR)
    @GetMapping("$INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$FEATURE_RELEASE_URL_SEGMENT/$EXAMPLE_FEATURE_FLAG_FOUR")
    fun featureReleaseStrategyInverseFlaggedEndpointFlagFour(model: Model): String {
        populateModelForFeatureReleaseTest(model, EXAMPLE_FEATURE_FLAG_FOUR, true)

        return "featureFlagExamples/releaseWithReleaseStrategyTest"
    }

    private fun getFeatureFlagModelFromConfig(featureName: String): FeatureFlagConfigModel =
        featureFlagConfig.featureFlags.firstOrNull { it.name == featureName }
            ?: throw IllegalArgumentException("Feature flag $featureName not found in config")

    private fun getFeatureReleaseModelFromConfig(releaseName: String): FeatureReleaseConfigModel =
        featureFlagConfig.releases.firstOrNull { it.name == releaseName }
            ?: throw IllegalArgumentException("Feature release $releaseName not found in config")

    private fun populateModelForFeatureReleaseTest(
        model: Model,
        flagName: String,
        inverseFlaggedEndpoint: Boolean = false,
    ) {
        val endpointAvailableWhenFlagIs = if (inverseFlaggedEndpoint) "DISABLED" else "ENABLED"

        val featureFlagSetInConfig = getFeatureFlagModelFromConfig(flagName)
        if (featureFlagSetInConfig.release == null) {
            throw IllegalArgumentException("Feature flag $flagName is not part of a feature release")
        }
        val featureReleaseSetInConfig = getFeatureReleaseModelFromConfig(featureFlagSetInConfig.release)
        val featureEnabledText =
            if (featureFlagManager.checkFeature(flagName)) {
                "This feature is ENABLED"
            } else {
                "This feature is DISABLED"
            }

        val releaseStrategyConfig = featureReleaseSetInConfig.strategyConfig
        val releaseDate = releaseStrategyConfig?.releaseDate?.toString() ?: null

        model.addAttribute("ffTestHeading", "Feature flagged controller endpoint - available when flag is $endpointAvailableWhenFlagIs")
        model.addAttribute("flagGroupName", featureFlagSetInConfig.release)
        model.addAttribute("ffSubHeading", "Configuration for $flagName")

        model.addAttribute(
            "flagValueInConfig",
            "Flag $flagName was initialised to ${featureFlagSetInConfig.enabled.toString().uppercase()} in FeatureFlagConfig",
        )
        model.addAttribute(
            "flagGroupValueInConfig",
            "Flag Group ${featureFlagSetInConfig.release} was initialised to " +
                "${featureReleaseSetInConfig.enabled.toString().uppercase()} in FeatureFlagConfig",
        )
        model.addAttribute("featureEnabled", featureEnabledText)

        model.addAttribute("releaseDate", releaseDate)
    }

    companion object {
        const val FEATURED_FLAGGED_SERVICE_TEST_URL_SEGMENT = "feature-flagged-service-test"
        const val FEATURED_FLAGGED_TEMPLATE_TEST_URL_SEGMENT = "feature-flagged-template-test"
        const val FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT = "feature-flagged-endpoint-test"
        const val INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT = "inverse-feature-flagged-endpoint-test"
        const val FEATURE_RELEASE_URL_SEGMENT = "feature-release"

        const val FEATURED_FLAGGED_SERVICE_TEST_URL_ROUTE = "/$LANDLORD_PATH_SEGMENT/$FEATURED_FLAGGED_SERVICE_TEST_URL_SEGMENT"
        const val FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE = "/$LANDLORD_PATH_SEGMENT/$FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT"
        const val INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT"
        const val FEATURED_FLAGGED_TEMPLATE_TEST_URL_ROUTE = "/$LANDLORD_PATH_SEGMENT/$FEATURED_FLAGGED_TEMPLATE_TEST_URL_SEGMENT"
        const val FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_2_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$FEATURE_RELEASE_URL_SEGMENT/$EXAMPLE_FEATURE_FLAG_TWO"
        const val INVERSE_FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_2_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/" +
                "$FEATURE_RELEASE_URL_SEGMENT/$EXAMPLE_FEATURE_FLAG_TWO"
        const val FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_3_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$FEATURE_RELEASE_URL_SEGMENT/$EXAMPLE_FEATURE_FLAG_THREE"
        const val INVERSE_FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_3_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT" +
                "/$FEATURE_RELEASE_URL_SEGMENT/$EXAMPLE_FEATURE_FLAG_THREE"
        const val FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT/$FEATURE_RELEASE_URL_SEGMENT/$EXAMPLE_FEATURE_FLAG_FOUR"
        const val INVERSE_FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT" +
                "/$FEATURE_RELEASE_URL_SEGMENT/$EXAMPLE_FEATURE_FLAG_FOUR"
    }
}
