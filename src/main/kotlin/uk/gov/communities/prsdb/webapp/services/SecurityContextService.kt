package uk.gov.communities.prsdb.webapp.services

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService

@PrsdbWebService
class SecurityContextService(
    private val securityContextRepository: SecurityContextRepository,
) {
    /* Saving a blank security context effectively "logs the user out", only from PRSDB web and not from one-login.
     * On the next request, the service will log the user back in via one-login without any interaction required
     * from the user, creating a fresh security context. The main purpose for this is updating their user roles.
     */
    fun refreshContext() {
        val attributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        val request = attributes.request
        val response = attributes.response

        securityContextRepository.saveContext(SecurityContextHolder.createEmptyContext(), request, response)
    }
}
