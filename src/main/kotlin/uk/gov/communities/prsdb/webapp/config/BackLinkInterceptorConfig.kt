package uk.gov.communities.prsdb.webapp.config

import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor

@Configuration
class BackLinkInterceptorConfig : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(BackLinkInterceptor { destination -> getCurrentUrl() })
    }
}

fun getCurrentUrl(): String? {
    val context = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?
    if (context?.request is HttpServletRequest) {
        return context.request.requestURI +
            context.request.queryString
                ?.let { "?$it" }
                .orEmpty()
    }
    return null
}
