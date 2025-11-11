package uk.gov.communities.prsdb.webapp.controllers

import org.ff4j.FF4j
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.services.interfaces.ExampleFeatureFlaggedService

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed
@PrsdbController
@RequestMapping("/$LANDLORD_PATH_SEGMENT")
class ExampleFeatureFlagTestController(
    private val exampleFeatureFlaggedService: ExampleFeatureFlaggedService,
    private val ff4j: FF4j,
) {
    @GetMapping("feature-flagged-service-test")
    fun index(model: Model): String {
        val configFlagValue =
            if (ff4j.check(EXAMPLE_FEATURE_FLAG_ONE)) {
                "Feature Flag in FF4JConfig is ON"
            } else {
                "Feature Flag in FF4JConfig is OFF"
            }
        model.addAttribute("ffTestHeading", exampleFeatureFlaggedService.getFeatureFlagPageHeading())
        model.addAttribute("ffConfigFeature", configFlagValue)

        // TODO PRSD-1683 - delete template when no longer needed
        return "featureFlagTest"
    }
}
