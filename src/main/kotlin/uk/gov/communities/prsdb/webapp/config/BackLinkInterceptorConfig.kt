package uk.gov.communities.prsdb.webapp.config

import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor
import uk.gov.communities.prsdb.webapp.services.BackUrlStorageService

@PrsdbWebConfiguration
class BackLinkInterceptorConfig(
    private val backUrlStorageService: BackUrlStorageService,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(BackLinkInterceptor { backUrlStorageService.getBackUrl(it) })
    }
}
