package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.MessageSource
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.config.interceptors.ServiceNameInterceptor
import java.util.Locale

@PrsdbWebConfiguration
class ServiceNameInterceptorConfig(
    private val messageSource: MessageSource,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        val localCouncilServiceName =
            messageSource.getMessage("localCouncilServiceName", null, "Check a rental property or landlord", Locale.getDefault())!!
        registry.addInterceptor(ServiceNameInterceptor(localCouncilServiceName))
    }
}
