package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.COOKIE_TOKENS
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class CookieServiceTests {
    @Mock
    private lateinit var mockHttpSession: HttpSession

    @InjectMocks
    private lateinit var cookieService: CookieService

    @Test
    fun `createCookieForValue creates a cookie with an issued token for the given inputs`() {
        val cookieName = "name"
        val cookiePath = "/path"
        val cookieValue = "value"

        val createdCookie = cookieService.createCookieForValue(cookieName, cookiePath, cookieValue)

        verify(mockHttpSession).setAttribute(COOKIE_TOKENS, mapOf(createdCookie.value to cookieValue))
        assertEquals(cookieName, createdCookie.name)
        assertEquals(cookiePath, createdCookie.path)
        assertTrue(createdCookie.isHttpOnly)
        assertTrue(createdCookie.secure)
    }

    @Test
    fun `isTokenForCookieValue returns false if there are no tokens in session`() {
        whenever(mockHttpSession.getAttribute(COOKIE_TOKENS)).thenReturn(emptyMap<String, Any>())

        assertFalse(cookieService.isTokenForCookieValue("anyToken", "anyCookieValue"))
    }

    @Test
    fun `isTokenForCookieValue returns false if the token isn't in session`() {
        whenever(mockHttpSession.getAttribute(COOKIE_TOKENS)).thenReturn(mapOf("otherToken" to "cookieValue"))

        assertFalse(cookieService.isTokenForCookieValue("token", "cookieValue"))
    }

    @Test
    fun `isTokenForCookieValue returns false if the token isn't for the cookie value`() {
        val token = "token"
        whenever(mockHttpSession.getAttribute(COOKIE_TOKENS)).thenReturn(mapOf(token to "otherCookieValue"))

        assertFalse(cookieService.isTokenForCookieValue(token, "cookieValue"))
    }

    @Test
    fun `isTokenForCookieValue returns true if the token is for the cookie value`() {
        val token = "token"
        val cookieValue = "value"
        whenever(mockHttpSession.getAttribute(COOKIE_TOKENS)).thenReturn(mapOf(token to cookieValue))

        assertTrue(cookieService.isTokenForCookieValue(token, cookieValue))
    }

    @Test
    fun `useToken removes the token from session`() {
        val token = "token"
        whenever(mockHttpSession.getAttribute(COOKIE_TOKENS)).thenReturn(mapOf(token to "cookieValue", "otherToken" to "cookieValue"))

        cookieService.useToken(token)

        verify(mockHttpSession).setAttribute(COOKIE_TOKENS, mapOf("otherToken" to "cookieValue"))
    }
}
