package uk.gov.communities.prsdb.webapp.controllers

import org.ff4j.FF4j
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.FIRST_TOY_FEATURE_FLAG
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.services.interfaces.FeatureFlagTestService

@PrsdbController
@RequestMapping("/$LANDLORD_PATH_SEGMENT")
class FeatureFlagTestController(
    private val featureFlagTestService: FeatureFlagTestService,
    private val ff4j: FF4j,
) {
    @GetMapping("feature-flag-test")
    fun index(model: Model): String {
        val configFlagValue =
            if (ff4j.check(FIRST_TOY_FEATURE_FLAG)) {
                "Feature Flag in config is ON"
            } else {
                "Feature Flag in config is OFF"
            }
        model.addAttribute("ffTestHeading", featureFlagTestService.getFeatureFlagPageHeading())
        model.addAttribute("ffConfigFeature", configFlagValue)
        return "featureFlagTest"
    }
}
