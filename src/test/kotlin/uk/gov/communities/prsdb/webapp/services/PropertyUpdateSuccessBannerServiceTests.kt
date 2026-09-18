package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_UPDATE_SUCCESS_BANNER_SESSION_ATTRIBUTE

class PropertyUpdateSuccessBannerServiceTests {
    private val session: HttpSession = mock()
    private val service = PropertyUpdateSuccessBannerService(session)

    @Test
    fun `markSuccess stores the message key against the propertyId in the session`() {
        whenever(session.getAttribute(PROPERTY_UPDATE_SUCCESS_BANNER_SESSION_ATTRIBUTE)).thenReturn(null)

        service.markSuccess(propertyId = 1L, messageKey = "propertyDetails.updateSuccessBanner.licensing")

        org.mockito.kotlin.verify(session).setAttribute(
            PROPERTY_UPDATE_SUCCESS_BANNER_SESSION_ATTRIBUTE,
            mapOf(1L to "propertyDetails.updateSuccessBanner.licensing"),
        )
    }

    @Test
    fun `consumeSuccess returns the stored message key and removes it from the session`() {
        val existingMap = mutableMapOf(1L to "propertyDetails.updateSuccessBanner.licensing")
        whenever(session.getAttribute(PROPERTY_UPDATE_SUCCESS_BANNER_SESSION_ATTRIBUTE)).thenReturn(existingMap)

        val result = service.consumeSuccess(propertyId = 1L)

        assertEquals("propertyDetails.updateSuccessBanner.licensing", result)
        org.mockito.kotlin
            .verify(session)
            .setAttribute(PROPERTY_UPDATE_SUCCESS_BANNER_SESSION_ATTRIBUTE, emptyMap<Long, String>())
    }

    @Test
    fun `consumeSuccess returns null when no entry exists for the propertyId`() {
        whenever(session.getAttribute(PROPERTY_UPDATE_SUCCESS_BANNER_SESSION_ATTRIBUTE)).thenReturn(null)

        assertNull(service.consumeSuccess(propertyId = 1L))
    }

    @Test
    fun `consumeSuccess for one propertyId does not affect another propertyId's entry`() {
        val existingMap =
            mutableMapOf(
                1L to "propertyDetails.updateSuccessBanner.licensing",
                2L to "propertyDetails.updateSuccessBanner.gasSafety",
            )
        whenever(session.getAttribute(PROPERTY_UPDATE_SUCCESS_BANNER_SESSION_ATTRIBUTE)).thenReturn(existingMap)

        service.consumeSuccess(propertyId = 1L)

        org.mockito.kotlin.verify(session).setAttribute(
            PROPERTY_UPDATE_SUCCESS_BANNER_SESSION_ATTRIBUTE,
            mapOf(2L to "propertyDetails.updateSuccessBanner.gasSafety"),
        )
    }

    @Test
    fun `consumeSuccess called twice for the same propertyId returns null the second time`() {
        val existingMap = mutableMapOf(1L to "propertyDetails.updateSuccessBanner.licensing")
        whenever(session.getAttribute(PROPERTY_UPDATE_SUCCESS_BANNER_SESSION_ATTRIBUTE)).thenReturn(existingMap, emptyMap<Long, String>())

        service.consumeSuccess(propertyId = 1L)
        val secondResult = service.consumeSuccess(propertyId = 1L)

        assertNull(secondResult)
    }
}
