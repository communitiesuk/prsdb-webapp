package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uk.gov.communities.prsdb.webapp.constants.FEATURE_FLAG_OVERRIDES
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagOverrides

class FeatureFlagOverrideServiceTests {
    private lateinit var session: HttpSession

    @BeforeEach
    fun setUp() {
        session = mock()
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))
    }

    @AfterEach
    fun tearDown() {
        RequestContextHolder.resetRequestAttributes()
    }

    @Test
    fun `getOverrides returns the overrides stored in the session`() {
        val storedOverrides = FeatureFlagOverrides(flags = mapOf("some-flag" to true))
        whenever(session.getAttribute(FEATURE_FLAG_OVERRIDES)).thenReturn(storedOverrides)

        val service = FeatureFlagOverrideService(session, overridesEnabled = true)

        assertEquals(storedOverrides, service.getOverrides())
    }

    @Test
    fun `getOverrides returns empty overrides when the session holds nothing`() {
        whenever(session.getAttribute(FEATURE_FLAG_OVERRIDES)).thenReturn(null)

        val service = FeatureFlagOverrideService(session, overridesEnabled = true)

        assertTrue(service.getOverrides().isEmpty())
    }

    @Test
    fun `getOverrides returns empty overrides without reading the session when overrides are disabled`() {
        val service = FeatureFlagOverrideService(session, overridesEnabled = false)

        assertTrue(service.getOverrides().isEmpty())
        verify(session, never()).getAttribute(FEATURE_FLAG_OVERRIDES)
    }

    @Test
    fun `getOverrides returns empty overrides without reading the session when there is no request context`() {
        RequestContextHolder.resetRequestAttributes()

        val service = FeatureFlagOverrideService(session, overridesEnabled = true)

        assertTrue(service.getOverrides().isEmpty())
        verify(session, never()).getAttribute(FEATURE_FLAG_OVERRIDES)
    }

    @Test
    fun `setOverrides stores the overrides in the session`() {
        val overrides = FeatureFlagOverrides(releases = mapOf("some-release" to false))
        val service = FeatureFlagOverrideService(session, overridesEnabled = true)

        service.setOverrides(overrides)

        verify(session).setAttribute(FEATURE_FLAG_OVERRIDES, overrides)
    }

    @Test
    fun `setOverrides does not write to the session when overrides are disabled`() {
        val service = FeatureFlagOverrideService(session, overridesEnabled = false)

        service.setOverrides(FeatureFlagOverrides(flags = mapOf("some-flag" to true)))

        verify(session, never()).setAttribute(any(), any())
    }

    @Test
    fun `clearOverrides stores empty overrides in the session`() {
        val service = FeatureFlagOverrideService(session, overridesEnabled = true)

        service.clearOverrides()

        verify(session).setAttribute(FEATURE_FLAG_OVERRIDES, FeatureFlagOverrides())
    }

    @Test
    fun `hasActiveOverrides is false when no overrides are set`() {
        whenever(session.getAttribute(FEATURE_FLAG_OVERRIDES)).thenReturn(FeatureFlagOverrides())

        val service = FeatureFlagOverrideService(session, overridesEnabled = true)

        assertFalse(service.hasActiveOverrides())
    }

    @Test
    fun `hasActiveOverrides is true when an override is set`() {
        whenever(session.getAttribute(FEATURE_FLAG_OVERRIDES))
            .thenReturn(FeatureFlagOverrides(flags = mapOf("some-flag" to true)))

        val service = FeatureFlagOverrideService(session, overridesEnabled = true)

        assertTrue(service.hasActiveOverrides())
    }
}
