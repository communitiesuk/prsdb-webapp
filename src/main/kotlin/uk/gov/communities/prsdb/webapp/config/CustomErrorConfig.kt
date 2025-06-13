package uk.gov.communities.prsdb.webapp.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver
import uk.gov.communities.prsdb.webapp.annotations.WebComponent
import uk.gov.communities.prsdb.webapp.annotations.WebConfiguration

@WebConfiguration
class CustomErrorConfig {
    // This only needs to have a higher @Order precedence (lower number) than the default handler (0)
    // Other handlers e.g. controller specific handlers should take precedence over this
    @Order(-1)
    @WebComponent
    class MalformedGETRequestExceptionResolver : HandlerExceptionResolver {
        private val defaultHandlerExceptionResolver: DefaultHandlerExceptionResolver = DefaultHandlerExceptionResolver()

        override fun resolveException(
            request: HttpServletRequest,
            response: HttpServletResponse,
            handler: Any?,
            exception: java.lang.Exception,
        ): ModelAndView? {
            if (request.method == HttpMethod.GET.name() &&
                (exception is HandlerMethodValidationException || exception is MethodArgumentTypeMismatchException)
            ) {
                // This is the same as the internal handling of these exceptions in DefaultHandlerExceptionResolver
                // but replacing the status code 400 with a 404
                response.sendError(HttpServletResponse.SC_NOT_FOUND)
                return ModelAndView()
            }
            return defaultHandlerExceptionResolver.resolveException(request, response, handler, exception)
        }
    }
}
