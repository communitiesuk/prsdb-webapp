package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureFlagDisabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureFlagEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
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
        val configFlagValue =
            if (featureFlagManager.checkFeature(EXAMPLE_FEATURE_FLAG_ONE)) {
                "Feature Flag in FeatureFlagConfig is ON"
            } else {
                "Feature Flag in FeatureFlagConfig is OFF"
            }
        model.addAttribute("ffTestHeading", exampleFeatureFlaggedService.getFeatureFlagPageHeading())
        model.addAttribute("ffConfigFeature", configFlagValue)

        // TODO PRSD-1683 - delete template when no longer needed
        return "featureFlagTest"
    }

    @AvailableWhenFeatureFlagEnabled(EXAMPLE_FEATURE_FLAG_ONE)
    @GetMapping(FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT)
    fun featureFlaggedEndpointTest(model: Model): String {
        val configFlagValue =
            if (featureFlagManager.checkFeature(EXAMPLE_FEATURE_FLAG_ONE)) {
                "Feature Flag in FeatureFlagConfig is ON"
            } else {
                "Feature Flag in FeatureFlagConfig is OFF"
            }
        model.addAttribute("ffTestHeading", "Feature flagged controller endpoint - available when flag is ENABLED")
        model.addAttribute("ffConfigFeature", configFlagValue)

        return "featureFlagTest"
    }

    @AvailableWhenFeatureFlagDisabled(EXAMPLE_FEATURE_FLAG_ONE)
    @GetMapping(INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT)
    fun inverseFeatureFlaggedEndpointTest(model: Model): String {
        val configFlagValue =
            if (featureFlagManager.checkFeature(EXAMPLE_FEATURE_FLAG_ONE)) {
                "Feature Flag in FeatureFlagConfig is ON"
            } else {
                "Feature Flag in FeatureFlagConfig is OFF"
            }
        model.addAttribute("ffTestHeading", "Feature flagged controller endpoint - - available when flag is DISABLED")
        model.addAttribute("ffConfigFeature", configFlagValue)

        return "featureFlagTest"
    }

    companion object {
        const val FEATURED_FLAGGED_SERVICE_TEST_URL_SEGMENT = "feature-flagged-service-test"
        const val FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT = "feature-flagged-endpoint-test"
        const val INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT = "inverse-feature-flagged-endpoint-test"

        const val FEATURED_FLAGGED_SERVICE_TEST_URL_ROUTE = "/$LANDLORD_PATH_SEGMENT/$FEATURED_FLAGGED_SERVICE_TEST_URL_SEGMENT"
        const val FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE = "/$LANDLORD_PATH_SEGMENT/$FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT"
        const val INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_SEGMENT"
    }
}
