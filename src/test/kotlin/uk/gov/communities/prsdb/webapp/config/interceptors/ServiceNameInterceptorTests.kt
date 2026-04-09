package uk.gov.communities.prsdb.webapp.config.interceptors

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_PATH_SEGMENT
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class ServiceNameInterceptorTests {
    private val testServiceName = "Check a rental property or landlord"
    private val interceptor = ServiceNameInterceptor(testServiceName)

    @Test
    fun `postHandle sets customServiceName for local council routes`() {
        val request = MockHttpServletRequest()
        request.requestURI = "/$LOCAL_COUNCIL_PATH_SEGMENT/start"
        val modelAndView = ModelAndView("viewName")

        interceptor.postHandle(request, MockHttpServletResponse(), Any(), modelAndView)

        assertEquals(testServiceName, modelAndView.model["customServiceName"])
    }

    @Test
    fun `postHandle does not set customServiceName for non-local-council routes`() {
        val request = MockHttpServletRequest()
        request.requestURI = "/landlord/dashboard"
        val modelAndView = ModelAndView("viewName")

        interceptor.postHandle(request, MockHttpServletResponse(), Any(), modelAndView)

        assertNull(modelAndView.model["customServiceName"])
    }

    @Test
    fun `postHandle does not throw when modelAndView is null`() {
        val request = MockHttpServletRequest()
        request.requestURI = "/$LOCAL_COUNCIL_PATH_SEGMENT/start"

        interceptor.postHandle(request, MockHttpServletResponse(), Any(), null)
    }

    @Test
    fun `postHandle does not set customServiceName for redirect responses`() {
        val request = MockHttpServletRequest()
        request.requestURI = "/$LOCAL_COUNCIL_PATH_SEGMENT/some-action"
        val modelAndView = ModelAndView("redirect:/local-council/confirmation")

        interceptor.postHandle(request, MockHttpServletResponse(), Any(), modelAndView)

        assertNull(modelAndView.model["customServiceName"])
    }
}
