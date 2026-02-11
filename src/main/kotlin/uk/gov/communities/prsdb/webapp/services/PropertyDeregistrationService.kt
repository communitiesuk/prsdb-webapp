package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_DEREGISTERED_THIS_SESSION

@PrsdbWebService
class PropertyDeregistrationService(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val session: HttpSession,
) {
    fun deregisterProperty(propertyOwnershipId: Long) {
        propertyOwnershipService.deletePropertyOwnership(propertyOwnershipId)
    }

    fun addDeregisteredPropertyOwnershipIdToSession(propertyOwnershipId: Long) =
        session.setAttribute(
            PROPERTIES_DEREGISTERED_THIS_SESSION,
            getDeregisteredPropertyOwnershipIdsFromSession() + propertyOwnershipId,
        )

    @Suppress("UNCHECKED_CAST")
    fun getDeregisteredPropertyOwnershipIdsFromSession(): MutableList<Long> =
        session.getAttribute(PROPERTIES_DEREGISTERED_THIS_SESSION) as MutableList<Long>?
            ?: mutableListOf()
}
