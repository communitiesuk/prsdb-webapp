package uk.gov.communities.prsdb.webapp.services.interfaces

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_SINGLE_FEATURE_FLAG

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed
interface ExampleFeatureFlaggedService {
    @PrsdbFlip(name = EXAMPLE_SINGLE_FEATURE_FLAG, alterBean = "example-feature-flag-one-flag-on")
    fun getFeatureFlagPageHeading(): String

    @PrsdbFlip(name = EXAMPLE_SINGLE_FEATURE_FLAG, alterBean = "example-feature-flag-one-flag-on")
    fun getTemplateName(): String
}
