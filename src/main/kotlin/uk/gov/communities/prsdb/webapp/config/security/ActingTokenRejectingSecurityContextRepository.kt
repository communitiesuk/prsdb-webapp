package uk.gov.communities.prsdb.webapp.config.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.DeferredSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.web.context.SecurityContextRepository

class ActingTokenRejectingSecurityContextRepository(
    private val delegate: SecurityContextRepository,
) : SecurityContextRepository by delegate {
    override fun loadDeferredContext(request: HttpServletRequest): DeferredSecurityContext = delegate.loadDeferredContext(request)

    override fun saveContext(
        context: SecurityContext,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        check(context.authentication !is ActingAsLandlordAuthenticationToken) {
            "A security context holding an ActingAsLandlordAuthenticationToken must never be saved"
        }
        delegate.saveContext(context, request, response)
    }
}
