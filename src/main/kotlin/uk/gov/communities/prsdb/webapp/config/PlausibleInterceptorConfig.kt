package uk.gov.communities.prsdb.webapp.config

import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.config.interceptors.PlausibleInterceptor

@PrsdbWebConfiguration
class PlausibleInterceptorConfig : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(PlausibleInterceptor())
    }
}
