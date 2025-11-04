package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.services.interfaces.FeatureFlagTestService

@PrsdbWebService("flag-on")
class FeatureFlagTestServiceImpFlagOn : FeatureFlagTestService {
    override fun getFeatureFlagPageHeading() = "Feature Flag is ON"
}
