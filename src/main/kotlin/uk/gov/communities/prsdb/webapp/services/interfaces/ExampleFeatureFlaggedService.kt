package uk.gov.communities.prsdb.webapp.services.interfaces

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE

interface ExampleFeatureFlaggedService {
    @PrsdbFlip(name = EXAMPLE_FEATURE_FLAG_ONE, alterBean = "example-feature-flag-one-flag-on")
    fun getFeatureFlagPageHeading(): String
}
