package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed

@PrsdbFlip(name = EXAMPLE_FEATURE_FLAG_ONE, alterBean = "example-flagged-controller-flag-on-impl")
interface ExampleFlaggedController {
    @GetMapping("/landlord/feature-flagged-endpoint-test")
    fun featureFlagTest(model: Model): String
}
