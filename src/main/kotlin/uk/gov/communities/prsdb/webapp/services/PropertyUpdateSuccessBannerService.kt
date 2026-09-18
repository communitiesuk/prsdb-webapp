package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_UPDATE_SUCCESS_BANNER_SESSION_ATTRIBUTE

/**
 * Session-backed record of which properties have an unread update-success banner
 * to display, keyed by propertyId, so multiple properties/tabs within the same
 * browser session are tracked independently.
 */
@PrsdbWebService
class PropertyUpdateSuccessBannerService(
    private val session: HttpSession,
) {
    fun markSuccess(
        propertyId: Long,
        messageKey: String,
    ) {
        val currentMap = getStoredMap()
        session.setAttribute(PROPERTY_UPDATE_SUCCESS_BANNER_SESSION_ATTRIBUTE, currentMap + (propertyId to messageKey))
    }

    fun consumeSuccess(propertyId: Long): String? {
        val currentMap = getStoredMap()
        val messageKey = currentMap[propertyId] ?: return null
        session.setAttribute(PROPERTY_UPDATE_SUCCESS_BANNER_SESSION_ATTRIBUTE, currentMap - propertyId)
        return messageKey
    }

    @Suppress("UNCHECKED_CAST")
    private fun getStoredMap(): Map<Long, String> =
        (session.getAttribute(PROPERTY_UPDATE_SUCCESS_BANNER_SESSION_ATTRIBUTE) as? Map<Long, String>) ?: emptyMap()
}
