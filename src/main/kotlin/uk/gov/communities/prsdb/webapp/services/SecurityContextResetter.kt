package uk.gov.communities.prsdb.webapp.services

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Service
class SecurityContextResetter(
    private val securityContextRepository: SecurityContextRepository,
) {
    fun reset() {
        val attributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        val request = attributes.request
        val response = attributes.response
        securityContextRepository.saveContext(SecurityContextHolder.createEmptyContext(), request, response)
    }
}
