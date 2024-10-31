package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.multipageforms.JourneyData

// Note, this service is **REQUEST SCOPED** i.e. it is not a singleton. This is to ensure the per-request HttpSession
// is injected
@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
class MultiPageFormSessionService(
    private val session: HttpSession,
) {
    @Suppress("UNCHECKED_CAST")
    fun getJourneyData(): JourneyData = session.getAttribute("journeyData") as? JourneyData ?: mutableMapOf()

    fun setJourneyData(journeyData: JourneyData) {
        session.setAttribute("journeyData", journeyData)
    }

    fun getContextId(): Long? = session.getAttribute("contextId") as? Long

    fun setContextId(contextId: Long) {
        session.setAttribute("contextId", contextId)
    }
}
