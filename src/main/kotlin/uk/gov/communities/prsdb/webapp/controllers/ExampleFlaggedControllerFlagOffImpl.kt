package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.context.annotation.Primary
import org.springframework.ui.Model
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE

@Primary
@PrsdbController("example-flagged-controller-flag-off-impl")
class ExampleFlaggedControllerFlagOffImpl(
    val featureFlagManager: FeatureFlagManager,
) : ExampleFlaggedController {
    override fun featureFlagTest(model: Model): String {
        // TODO PRSD-1647 - make this throw Page Not Found
        val configFlagValue =
            if (featureFlagManager.check(EXAMPLE_FEATURE_FLAG_ONE)) {
                "Feature Flag in FeatureFlagConfig is ON"
            } else {
                "Feature Flag in FeatureFlagConfig is OFF"
            }
        model.addAttribute("ffTestHeading", "Feature flagged controller endpoint - INACTIVE")
        model.addAttribute("ffConfigFeature", configFlagValue)

        return "featureFlagTest"
    }
}
