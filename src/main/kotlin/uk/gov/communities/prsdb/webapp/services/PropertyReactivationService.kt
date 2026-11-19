package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.REACTIVATED_PROPERTY_OWNERSHIP_ID

@PrsdbWebService
class PropertyReactivationService(
    private val activeInactiveService: ActiveInactiveService,
    private val session: HttpSession,
) {
    @Transactional
    fun reactivateProperty(propertyOwnershipId: Long) {
        activeInactiveService.reactivateProperty(propertyOwnershipId)
        session.setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
    }

    fun getLastPropertyOwnershipIdReactivatedThisSession(): Long? = session.getAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID) as? Long
}
