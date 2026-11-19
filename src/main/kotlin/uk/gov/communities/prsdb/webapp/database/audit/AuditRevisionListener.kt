package uk.gov.communities.prsdb.webapp.database.audit

import org.hibernate.envers.RevisionListener
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import uk.gov.communities.prsdb.webapp.config.interceptors.LettingAgentAccessInterceptor
import uk.gov.communities.prsdb.webapp.database.entity.AuditRevision

class AuditRevisionListener : RevisionListener {
    override fun newRevision(revisionEntity: Any) {
        val revision = revisionEntity as AuditRevision
        revision.userSubjectIdentifier = currentUserSubjectIdentifier()
        revision.lettingAgentAccessId = currentLettingAgentAccessId()
    }

    private fun currentUserSubjectIdentifier(): String? =
        SecurityContextHolder
            .getContext()
            .authentication
            ?.takeIf { it.isAuthenticated && it !is AnonymousAuthenticationToken }
            ?.name

    private fun currentLettingAgentAccessId(): Long? =
        RequestContextHolder
            .getRequestAttributes()
            ?.getAttribute(LettingAgentAccessInterceptor.LETTING_AGENT_ACCESS_ID_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST)
            as? Long
}
