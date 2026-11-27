package uk.gov.communities.prsdb.webapp.config.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import uk.gov.communities.prsdb.webapp.services.ActingAsLandlordService
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ActingAsLandlordRedirectFilterTests {
    private val actingAsLandlordService: ActingAsLandlordService = mock()
    private val filter = ActingAsLandlordRedirectFilter(actingAsLandlordService)

    private var chainInvoked = false
    private val chain = FilterChain { _, _ -> chainInvoked = true }

    @Test
    fun `doFilter redirects a landlord route to the acting route while acting`() {
        // Arrange
        whenever(actingAsLandlordService.isActingAsLandlord()).thenReturn(true)
        val response = MockHttpServletResponse()

        // Act
        filter.doFilter(MockHttpServletRequest("GET", LANDLORD_ROUTE), response, chain)

        // Assert
        assertEquals(HttpServletResponse.SC_TEMPORARY_REDIRECT, response.status)
        assertEquals("/system-operator$LANDLORD_ROUTE", response.getHeader(HttpHeaders.LOCATION))
        assertFalse(chainInvoked)
    }

    @Test
    fun `doFilter keeps the query string when redirecting while acting`() {
        // Arrange
        whenever(actingAsLandlordService.isActingAsLandlord()).thenReturn(true)
        val request = MockHttpServletRequest("GET", LANDLORD_ROUTE).apply { queryString = QUERY_STRING }
        val response = MockHttpServletResponse()

        // Act
        filter.doFilter(request, response, chain)

        // Assert
        assertEquals(HttpServletResponse.SC_TEMPORARY_REDIRECT, response.status)
        assertEquals("/system-operator$LANDLORD_ROUTE?$QUERY_STRING", response.getHeader(HttpHeaders.LOCATION))
        assertFalse(chainInvoked)
    }

    @Test
    fun `doFilter passes the request through when not acting`() {
        // Arrange
        whenever(actingAsLandlordService.isActingAsLandlord()).thenReturn(false)
        val response = MockHttpServletResponse()

        // Act
        filter.doFilter(MockHttpServletRequest("GET", LANDLORD_ROUTE), response, chain)

        // Assert
        assertTrue(chainInvoked)
        assertNull(response.getHeader(HttpHeaders.LOCATION))
    }

    @Test
    fun `doFilter passes the landlord login callback through while acting`() {
        // Arrange
        whenever(actingAsLandlordService.isActingAsLandlord()).thenReturn(true)
        val response = MockHttpServletResponse()

        // Act
        filter.doFilter(MockHttpServletRequest("GET", LANDLORD_LOGIN_CALLBACK_ROUTE), response, chain)

        // Assert
        assertTrue(chainInvoked)
        assertNull(response.getHeader(HttpHeaders.LOCATION))
    }

    companion object {
        private const val LANDLORD_ROUTE = "/landlord/dashboard"
        private const val LANDLORD_LOGIN_CALLBACK_ROUTE = "/landlord/login/oauth2/code/one-login"
        private const val QUERY_STRING = "foo=bar&baz=qux"
    }
}
