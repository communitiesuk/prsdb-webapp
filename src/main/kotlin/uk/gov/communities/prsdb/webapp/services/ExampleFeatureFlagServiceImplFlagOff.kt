package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.services.interfaces.ExampleFeatureFlaggedService

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed
@Primary
@PrsdbWebService("example-feature-flag-one-flag-off")
class ExampleFeatureFlagServiceImplFlagOff : ExampleFeatureFlaggedService {
    override fun getFeatureFlagPageHeading() = "Using ExampleFeatureFlaggedService - Flag OFF"
}
