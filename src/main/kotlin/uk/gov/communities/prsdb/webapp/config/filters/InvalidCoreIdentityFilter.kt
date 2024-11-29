package uk.gov.communities.prsdb.webapp.config.filters

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.SecurityContextRepository
import uk.gov.communities.prsdb.webapp.exceptions.InvalidCoreIdentityException

class InvalidCoreIdentityFilter(
    private val securityContextRepository: SecurityContextRepository,
) : Filter {
    override fun doFilter(
        request: ServletRequest?,
        response: ServletResponse?,
        chain: FilterChain,
    ) {
        doFilter(request as HttpServletRequest, response as HttpServletResponse, chain)
    }

    private fun doFilter(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        try {
            chain.doFilter(request, response)
        } catch (e: ServletException) {
            val cause = e.cause
            if (cause is InvalidCoreIdentityException) {
                securityContextRepository.saveContext(SecurityContextHolder.createEmptyContext(), request, response)
            }

            throw e
        }
    }
}
