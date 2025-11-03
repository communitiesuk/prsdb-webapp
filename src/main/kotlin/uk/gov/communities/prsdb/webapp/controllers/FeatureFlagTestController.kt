package uk.gov.communities.prsdb.webapp.controllers

import org.ff4j.FF4j
import org.ff4j.aop.Flip
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.FIRST_TOY_FEATURE_FLAG
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.services.interfaces.FeatureFlagTestServiceInterface

@PrsdbController
@RequestMapping("/$LANDLORD_PATH_SEGMENT")
@Flip(name = FIRST_TOY_FEATURE_FLAG)
class FeatureFlagTestController(
    private val featureFlagTestServiceInterface: FeatureFlagTestServiceInterface,
    private val ff4j: FF4j,
) {
    @GetMapping("feature-flag-test")
    fun index(model: Model): String {
        model.addAttribute("ffTestHeading", featureFlagTestServiceInterface.getFeatureFlagPageHeading())
        model.addAttribute("ffControllerFeatures", ff4j.features)
        return "featureFlagTest"
    }
}
