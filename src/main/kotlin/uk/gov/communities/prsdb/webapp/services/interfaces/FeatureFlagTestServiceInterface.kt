package uk.gov.communities.prsdb.webapp.services.interfaces

import org.ff4j.aop.Flip
import uk.gov.communities.prsdb.webapp.constants.FIRST_TOY_FEATURE_FLAG

interface FeatureFlagTestServiceInterface {
    @Flip(name = FIRST_TOY_FEATURE_FLAG, alterBean = "flag-on")
    fun getFeatureFlagPageHeading(): String
}
