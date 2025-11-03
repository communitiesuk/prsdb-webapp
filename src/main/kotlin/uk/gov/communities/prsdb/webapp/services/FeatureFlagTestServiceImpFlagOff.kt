package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.services.interfaces.FeatureFlagTestServiceInterface

@PrsdbWebComponent("flag-off")
@Primary
class FeatureFlagTestServiceImpFlagOff : FeatureFlagTestServiceInterface {
    override fun getFeatureFlagPageHeading() = "Feature Flag is OFF"
}
