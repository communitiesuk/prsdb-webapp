package uk.gov.communities.prsdb.webapp.services.interfaces

import org.ff4j.aop.Flip
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE

interface ExampleFeatureFlaggedService {
    @Flip(name = EXAMPLE_FEATURE_FLAG_ONE, alterBean = "example-feature-flag-one-flag-on")
    fun getFeatureFlagPageHeading(): String
}
