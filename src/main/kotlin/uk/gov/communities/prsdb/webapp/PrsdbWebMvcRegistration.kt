package uk.gov.communities.prsdb.webapp

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.config.resolvers.FeatureFlagHandlerMapping

// There should be at most one WebMvcRegistrations bean in the application
@Component
class PrsdbWebMvcRegistration(
    private val featureFlagManager: FeatureFlagManager,
) : WebMvcRegistrations {
    override fun getRequestMappingHandlerMapping(): RequestMappingHandlerMapping = FeatureFlagHandlerMapping(featureFlagManager)
}
