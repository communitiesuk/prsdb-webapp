package uk.gov.communities.prsdb.webapp.config.interceptors

import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RobotsTagInterceptorTests {
    @Test
    fun `preHandle adds a noindex nofollow X-Robots-Tag header and allows the request to continue`() {
        val response = MockHttpServletResponse()

        val result = RobotsTagInterceptor().preHandle(MockHttpServletRequest(), response, Any())

        assertTrue(result)
        assertEquals("noindex, nofollow", response.getHeader("X-Robots-Tag"))
    }
}
