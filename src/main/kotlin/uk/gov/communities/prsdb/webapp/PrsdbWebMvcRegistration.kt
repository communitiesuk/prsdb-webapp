package uk.gov.communities.prsdb.webapp

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import uk.gov.communities.prsdb.webapp.config.resolvers.FeatureFlagHandlerMapping
import uk.gov.communities.prsdb.webapp.services.FeatureFlagChecker

@Component
class PrsdbWebMvcRegistration(
    private val featureFlagChecker: FeatureFlagChecker,
) : WebMvcRegistrations {
    override fun getRequestMappingHandlerMapping(): RequestMappingHandlerMapping = FeatureFlagHandlerMapping(featureFlagChecker)
}
