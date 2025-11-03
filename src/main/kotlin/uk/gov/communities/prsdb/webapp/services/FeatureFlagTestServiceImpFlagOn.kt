package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.services.interfaces.FeatureFlagTestServiceInterface

@PrsdbWebComponent("flag-on")
class FeatureFlagTestServiceImpFlagOn : FeatureFlagTestServiceInterface {
    override fun getFeatureFlagPageHeading() = "Feature Flag is ON"
}
