package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.services.interfaces.ExampleFeatureFlaggedService

@PrsdbWebService("example-feature-flag-one-flag-on")
class ExampleFeatureFlagServiceImpFlagOn : ExampleFeatureFlaggedService {
    override fun getFeatureFlagPageHeading() = "Using ExampleFeatureFlaggedService - Flag ON"
}
