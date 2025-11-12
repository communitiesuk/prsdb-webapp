package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed

interface ExampleFlaggedController {
    @PrsdbFlip(name = EXAMPLE_FEATURE_FLAG_ONE, alterBean = "example-flagged-controller-flag-on-impl")
    @GetMapping("$LANDLORD_PATH_SEGMENT/feature-flagged-endpoint-test")
    fun featureFlagTest(model: Model): String
}
