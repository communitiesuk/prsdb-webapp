package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.services.interfaces.ExampleFeatureFlaggedService

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed
@PrsdbWebService("example-feature-flag-one-flag-on")
class ExampleFeatureFlagServiceImplFlagOn : ExampleFeatureFlaggedService {
    override fun getFeatureFlagPageHeading() = "Using ExampleFeatureFlaggedService - Flag ON"

    override fun getTemplateName(): String = "enabledFeature"
}
