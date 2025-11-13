package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE

@PrsdbController("example-flagged-controller-flag-on-impl")
class ExampleFlaggedControllerFlagOnImpl(
    val featureFlagManager: FeatureFlagManager,
) : ExampleFlaggedController {
    override fun featureFlagTest(model: Model): String {
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
