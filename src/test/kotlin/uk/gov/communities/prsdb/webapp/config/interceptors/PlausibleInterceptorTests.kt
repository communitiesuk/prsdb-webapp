package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class PlausibleInterceptorTests {
    private val mockRequest = MockHttpServletRequest()

    @Mock
    private lateinit var mockResponse: HttpServletResponse

    @InjectMocks
    private lateinit var plausibleInterceptor: PlausibleInterceptor

    private fun callPostHandle(modelAndView: ModelAndView?) =
        plausibleInterceptor.postHandle(mockRequest, mockResponse, Any(), modelAndView)

    private fun newModelAndView(viewName: String? = "some-view"): ModelAndView = ModelAndView().apply { this.viewName = viewName }

    @Test
    fun `postHandle adds currentUrl model attribute from request URI`() {
        mockRequest.requestURI = "/landlord/register-as-a-landlord/name"
        val modelAndView = newModelAndView()
        callPostHandle(modelAndView)
        assertEquals("/landlord/register-as-a-landlord/name", modelAndView.modelMap["plausibleEventCurrentUrl"])
    }

    @Test
    fun `postHandle currentUrl excludes query string`() {
        mockRequest.requestURI = "/landlord/register-as-a-landlord/name"
        mockRequest.queryString = "foo=bar&baz=qux"
        val modelAndView = newModelAndView()
        callPostHandle(modelAndView)
        assertEquals("/landlord/register-as-a-landlord/name", modelAndView.modelMap["plausibleEventCurrentUrl"])
    }

    @Test
    fun `postHandle sets referrer from Referer header path only`() {
        mockRequest.requestURI = "/page-b"
        mockRequest.serverName = "example.com"
        mockRequest.addHeader("Referer", "https://example.com/page-a")
        val modelAndView = newModelAndView()
        callPostHandle(modelAndView)
        assertEquals("/page-a", modelAndView.modelMap["plausibleEventReferrer"])
    }

    @Test
    fun `postHandle referrer excludes query string`() {
        mockRequest.requestURI = "/page-b"
        mockRequest.serverName = "example.com"
        mockRequest.addHeader("Referer", "https://example.com/page-a?token=abc")
        val modelAndView = newModelAndView()
        callPostHandle(modelAndView)
        assertEquals("/page-a", modelAndView.modelMap["plausibleEventReferrer"])
    }

    @Test
    fun `postHandle sets referrer to external when Referer header is missing`() {
        mockRequest.requestURI = "/page"
        val modelAndView = newModelAndView()
        callPostHandle(modelAndView)
        assertEquals("external", modelAndView.modelMap["plausibleEventReferrer"])
    }

    @Test
    fun `postHandle sets referrer to external when Referer header is malformed`() {
        mockRequest.requestURI = "/page"
        mockRequest.addHeader("Referer", "::not a url::")
        val modelAndView = newModelAndView()
        callPostHandle(modelAndView)
        assertEquals("external", modelAndView.modelMap["plausibleEventReferrer"])
    }

    @Test
    fun `postHandle sets referrer to external when Referer host differs from request host`() {
        mockRequest.requestURI = "/page"
        mockRequest.serverName = "example.com"
        mockRequest.addHeader("Referer", "https://other-site.test/some/path")
        val modelAndView = newModelAndView()
        callPostHandle(modelAndView)
        assertEquals("external", modelAndView.modelMap["plausibleEventReferrer"])
    }

    @Test
    fun `postHandle sets referrer to external when Referer is a hostless relative path`() {
        mockRequest.requestURI = "/page"
        mockRequest.addHeader("Referer", "/page-a")
        val modelAndView = newModelAndView()
        callPostHandle(modelAndView)
        assertEquals("external", modelAndView.modelMap["plausibleEventReferrer"])
    }

    @Test
    fun `postHandle sets referrer to external when Referer is a bare token`() {
        mockRequest.requestURI = "/page"
        mockRequest.addHeader("Referer", "page-a")
        val modelAndView = newModelAndView()
        callPostHandle(modelAndView)
        assertEquals("external", modelAndView.modelMap["plausibleEventReferrer"])
    }

    @Test
    fun `postHandle sets referrer to external when Referer host-only string has no scheme`() {
        mockRequest.requestURI = "/page"
        mockRequest.addHeader("Referer", "example.com/page-a")
        val modelAndView = newModelAndView()
        callPostHandle(modelAndView)
        assertEquals("external", modelAndView.modelMap["plausibleEventReferrer"])
    }

    @Test
    fun `postHandle normalises root-path same-host referrer to forward slash`() {
        mockRequest.requestURI = "/page"
        mockRequest.serverName = "example.com"
        mockRequest.addHeader("Referer", "https://example.com")
        val modelAndView = newModelAndView()
        callPostHandle(modelAndView)
        assertEquals("/", modelAndView.modelMap["plausibleEventReferrer"])
    }

    @Test
    fun `postHandle uses ERROR_REQUEST_URI when present for currentUrl`() {
        mockRequest.requestURI = "/error"
        mockRequest.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/landlord/some-broken-page")
        val modelAndView = newModelAndView()
        callPostHandle(modelAndView)
        assertEquals("/landlord/some-broken-page", modelAndView.modelMap["plausibleEventCurrentUrl"])
    }

    @Test
    fun `postHandle does nothing when modelAndView is null`() {
        mockRequest.requestURI = "/page"
        callPostHandle(null)
    }

    @Test
    fun `postHandle does not add attributes when view is a redirect prefix`() {
        mockRequest.requestURI = "/page"
        val modelAndView = newModelAndView(viewName = "redirect:/somewhere")
        callPostHandle(modelAndView)
        assertNull(modelAndView.modelMap["plausibleEventCurrentUrl"])
        assertNull(modelAndView.modelMap["plausibleEventReferrer"])
    }

    @Test
    fun `postHandle does not add attributes when view is a forward prefix`() {
        mockRequest.requestURI = "/page"
        val modelAndView = newModelAndView(viewName = "forward:/somewhere")
        callPostHandle(modelAndView)
        assertNull(modelAndView.modelMap["plausibleEventCurrentUrl"])
        assertNull(modelAndView.modelMap["plausibleEventReferrer"])
    }

    @Test
    fun `postHandle does not add attributes when view is a RedirectView instance`() {
        mockRequest.requestURI = "/page"
        val modelAndView = ModelAndView().apply { view = RedirectView("/somewhere") }
        callPostHandle(modelAndView)
        assertNull(modelAndView.modelMap["plausibleEventCurrentUrl"])
        assertNull(modelAndView.modelMap["plausibleEventReferrer"])
    }
}
