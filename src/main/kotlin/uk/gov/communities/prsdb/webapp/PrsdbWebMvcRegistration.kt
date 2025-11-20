package uk.gov.communities.prsdb.webapp

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.config.mappings.FeatureFlagConditionMapping

// There should be at most one WebMvcRegistrations bean in the application
@PrsdbWebComponent
class PrsdbWebMvcRegistration(
    private val featureFlagManager: FeatureFlagManager,
) : WebMvcRegistrations {
    override fun getRequestMappingHandlerMapping(): RequestMappingHandlerMapping = FeatureFlagConditionMapping(featureFlagManager)
}
