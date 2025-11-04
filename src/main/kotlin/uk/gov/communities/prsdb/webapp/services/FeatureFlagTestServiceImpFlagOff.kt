package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.services.interfaces.FeatureFlagTestService

@Primary
@PrsdbWebService("flag-off")
class FeatureFlagTestServiceImpFlagOff : FeatureFlagTestService {
    override fun getFeatureFlagPageHeading() = "Feature Flag is OFF"
}
