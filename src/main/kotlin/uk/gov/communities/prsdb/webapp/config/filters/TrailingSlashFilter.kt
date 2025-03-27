package uk.gov.communities.prsdb.webapp.config.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.filter.UrlHandlerFilter
import java.io.IOException

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class TrailingSlashFilter : OncePerRequestFilter() {
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Wraps any request with a url that ends in a '/' in a new request without that trailing slash
        val filter: UrlHandlerFilter = UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build()
        filter.doFilter(request, response, filterChain)
    }
}