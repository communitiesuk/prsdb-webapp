package uk.gov.communities.prsdb.webapp.config

import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.ui.ModelMap
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor
import uk.gov.communities.prsdb.webapp.constants.WITH_BACK_URL_PARAMETER_NAME
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class BackLinkInterceptorTests {
    @Test
    fun `postHandle sets a back url if the urlParameter has been set`() {
        val request: HttpServletRequest = mock()
        whenever(request.getParameter(WITH_BACK_URL_PARAMETER_NAME)).thenReturn("123")

        val modelAndView: ModelAndView = mock()
        val modelMap = ModelMap()
        whenever(modelAndView.modelMap).thenReturn(modelMap)

        val backLinkInterceptor =
            BackLinkInterceptor { destination ->
                when (destination) {
                    123 -> "http://example.com/back"
                    else -> null
                }
            }

        backLinkInterceptor.postHandle(
            request,
            mock(),
            mock(),
            modelAndView,
        )

        assertEquals(
            "http://example.com/back",
            modelAndView.modelMap["backUrl"],
        )
    }

    @Test
    fun `postHandle overrides back url if the urlParameter has been set`() {
        val request: HttpServletRequest = mock()
        whenever(request.getParameter(WITH_BACK_URL_PARAMETER_NAME)).thenReturn("123")

        val modelAndView: ModelAndView = mock()
        val modelMap = ModelMap()
        modelMap["backUrl"] = "http://example.com/old-back"
        whenever(modelAndView.modelMap).thenReturn(modelMap)

        val backLinkInterceptor =
            BackLinkInterceptor { destination ->
                when (destination) {
                    123 -> "http://example.com/back"
                    else -> null
                }
            }

        backLinkInterceptor.postHandle(
            request,
            mock(),
            mock(),
            modelAndView,
        )

        assertEquals(
            "http://example.com/back",
            modelAndView.modelMap["backUrl"],
        )
    }

    @Test
    fun `postHandle does not set the back url if the urlParameter has not been set`() {
        val request: HttpServletRequest = mock()
        whenever(request.getParameter(WITH_BACK_URL_PARAMETER_NAME)).thenReturn(null)

        val initialBackUrl = "http://example.com/old-back"
        val modelAndView: ModelAndView = mock()
        val modelMap = ModelMap()
        modelMap["backUrl"] = initialBackUrl
        whenever(modelAndView.modelMap).thenReturn(modelMap)

        val backLinkInterceptor = BackLinkInterceptor { _ -> null }

        backLinkInterceptor.postHandle(
            request,
            mock(),
            mock(),
            modelAndView,
        )

        assertEquals(initialBackUrl, modelAndView.modelMap["backUrl"])
    }

    @Test
    fun `postHandle does not override the back url if the urlParameter has not been set`() {
        val request: HttpServletRequest = mock()
        whenever(request.getParameter(WITH_BACK_URL_PARAMETER_NAME)).thenReturn(null)

        val modelAndView: ModelAndView = mock()
        val modelMap = ModelMap()
        whenever(modelAndView.modelMap).thenReturn(modelMap)

        val backLinkInterceptor = BackLinkInterceptor { _ -> null }

        backLinkInterceptor.postHandle(
            request,
            mock(),
            mock(),
            modelAndView,
        )

        assertNull(modelAndView.modelMap["backUrl"])
    }

    @Test
    fun `postHandle does not set the back url if the urlParameter does not correspond to a saved url`() {
        val request: HttpServletRequest = mock()
        whenever(request.getParameter(WITH_BACK_URL_PARAMETER_NAME)).thenReturn("123")

        val modelAndView: ModelAndView = mock()
        val modelMap = ModelMap()
        whenever(modelAndView.modelMap).thenReturn(modelMap)

        val backLinkInterceptor = BackLinkInterceptor { _ -> null }

        backLinkInterceptor.postHandle(
            request,
            mock(),
            mock(),
            modelAndView,
        )

        assertNull(modelAndView.modelMap["backUrl"])
    }

    @Test
    fun `postHandle forwards the withBackUrl urlParameter to view name redirects`() {
        val request: HttpServletRequest = mock()
        whenever(request.getParameter("withBackUrl")).thenReturn("123")

        val modelAndView: ModelAndView = mock()
        val modelMap = ModelMap()
        whenever(modelAndView.modelMap).thenReturn(modelMap)
        whenever(modelAndView.viewName).thenReturn("redirect:modelMap")

        val backLinkInterceptor = BackLinkInterceptor { _ -> null }

        backLinkInterceptor.postHandle(
            request,
            mock(),
            mock(),
            modelAndView,
        )

        assertEquals(modelAndView.modelMap["withBackUrl"], 123)
    }

    @Test
    fun `postHandle forwards the withBackUrl urlParameter to view name forwards`() {
        val request: HttpServletRequest = mock()
        whenever(request.getParameter("withBackUrl")).thenReturn("123")

        val modelAndView: ModelAndView = mock()
        val modelMap = ModelMap()
        whenever(modelAndView.modelMap).thenReturn(modelMap)
        whenever(modelAndView.viewName).thenReturn("forward:modelMap")

        val backLinkInterceptor = BackLinkInterceptor { _ -> null }

        backLinkInterceptor.postHandle(
            request,
            mock(),
            mock(),
            modelAndView,
        )

        assertEquals(modelAndView.modelMap["withBackUrl"], 123)
    }

    @Test
    fun `postHandle forwards the withBackUrl urlParameter to RedirectView redirects`() {
        val request: HttpServletRequest = mock()
        whenever(request.getParameter("withBackUrl")).thenReturn("123")

        val modelAndView: ModelAndView = mock()
        val modelMap = ModelMap()
        whenever(modelAndView.modelMap).thenReturn(modelMap)
        whenever(modelAndView.view).thenReturn(RedirectView("modelMap"))

        val backLinkInterceptor = BackLinkInterceptor { _ -> null }

        backLinkInterceptor.postHandle(
            request,
            mock(),
            mock(),
            modelAndView,
        )

        assertEquals(modelAndView.modelMap["withBackUrl"], 123)
    }
}
