package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbConditionalOnFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE

@PrsdbController
@PrsdbConditionalOnFeatureEnabled(EXAMPLE_FEATURE_FLAG_ONE)
class ExampleFlaggedController(
    val featureFlagManager: FeatureFlagManager,
) {
    @GetMapping("/landlord/feature-flagged-endpoint-test")
    fun featureFlagTest(model: Model): String {
        val configFlagValue =
            if (featureFlagManager.check(EXAMPLE_FEATURE_FLAG_ONE)) {
                "Feature Flag in FeatureFlagConfig is ON"
            } else {
                "Feature Flag in FeatureFlagConfig is OFF"
            }
        model.addAttribute("ffTestHeading", "Feature flagged controller endpoint - ACTIVE")
        model.addAttribute("ffConfigFeature", configFlagValue)

        return "featureFlagTest"
    }
}
