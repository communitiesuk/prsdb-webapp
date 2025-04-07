package uk.gov.communities.prsdb.webapp.config.filters

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
class MultipartFormDataFilter : Filter {
    override fun doFilter(
        request: ServletRequest?,
        response: ServletResponse?,
        chain: FilterChain,
    ) {
        doFilter(request as HttpServletRequest, response as HttpServletResponse, chain)
    }

    private val csrfName = "_csrf" // There should be a better way to get this key programmatically to match ${_csrf.parameterName}

    private fun doFilter(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        if (JakartaServletFileUpload.isMultipartContent(request)) {
            val upload = JakartaServletFileUpload()
            val multipartItemIterator = upload.getItemIterator(request)

            if (multipartItemIterator.hasNext()) {
                val csrfItem = multipartItemIterator.next()
                if (csrfItem.isFormField && csrfItem.fieldName == csrfName) {
                    csrfItem.inputStream.use { input ->
                        val rawToken = input.reader().readText()
                        request.setAttribute("multipartItemIterator", multipartItemIterator)
                        chain.doFilter(CsrfProvidingRequestWrapper(request, rawToken), response)
                        return
                    }
                }
            }
            throw PrsdbWebException("Must have CSRF as first item on forms")
        } else {
            chain.doFilter(request, response)
        }
    }
}

class CsrfProvidingRequestWrapper(
    private val request: HttpServletRequest,
    private val csrf: String,
) : HttpServletRequest by request {
    override fun getParameter(p0: String?): String =
        if (p0 == "_csrf") {
            csrf
        } else {
            request.getParameter(p0)
        }
}
