package uk.gov.communities.prsdb.webapp.config

import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.config.interceptors.InactiveLandlordInterceptor
import uk.gov.communities.prsdb.webapp.config.interceptors.InterceptorOrder
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.services.LandlordActiveStatusService

@PrsdbWebConfiguration
class InactiveLandlordInterceptorConfig(
    private val landlordActiveStatusService: LandlordActiveStatusService,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry
            .addInterceptor(InactiveLandlordInterceptor(landlordActiveStatusService))
            .addPathPatterns("$LANDLORD_DETAILS_FOR_LANDLORD_ROUTE/**")
            .order(InterceptorOrder.REDIRECT.value)
            // "/**" also matches the record page itself, which an inactive landlord must still be able to view
            .excludePathPatterns(LANDLORD_DETAILS_FOR_LANDLORD_ROUTE)
    }
}
