package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.security.ActingAsLandlordAuthenticationToken
import uk.gov.communities.prsdb.webapp.constants.ACTING_AS_USER_ID

@PrsdbWebService
class ActingAsLandlordSessionService(
    private val session: HttpSession,
) {
    fun getActingAsUserId(): String? = session.getAttribute(ACTING_AS_USER_ID) as? String

    // TODO PDJB-1904: Use this to redirect /landlord/** requests to /system-operator/landlord/** while acting
    fun isActingAsLandlord(): Boolean = getActingAsUserId() != null

    // TODO PDJB-1907: Call this from the start acting endpoint, after resetting the session (PDJB-1902)
    fun setActingAsUserId(userId: String) = session.setAttribute(ACTING_AS_USER_ID, userId)

    fun clearActingAsUserId() = session.removeAttribute(ACTING_AS_USER_ID)

    fun getActingSystemOperatorId(): String? =
        (SecurityContextHolder.getContext().authentication as? ActingAsLandlordAuthenticationToken)?.systemOperatorId
}
