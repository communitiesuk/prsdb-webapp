package uk.gov.communities.prsdb.webapp.config.filters

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.fileupload2.core.FileItemInputIterator
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload
import org.springframework.security.web.csrf.CsrfTokenRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException

class MultipartFormDataFilter(
    private val tokenRepository: CsrfTokenRepository,
) : Filter {
    override fun doFilter(
        request: ServletRequest?,
        response: ServletResponse?,
        chain: FilterChain,
    ) {
        doFilter(request as HttpServletRequest, response as HttpServletResponse, chain)
    }

    private fun doFilter(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        if (JakartaServletFileUpload.isMultipartContent(request)) {
            doFilterOnMultipartRequest(request, response, chain)
        } else {
            chain.doFilter(request, response)
        }
    }

    private fun doFilterOnMultipartRequest(
        multipartRequest: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        val upload = JakartaServletFileUpload()
        val multipartItemIterator = upload.getItemIterator(multipartRequest)
        multipartRequest.setAttribute(ITERATOR_ATTRIBUTE, multipartItemIterator)

        val tokenDetails = getCsrfTokenDetails(multipartRequest, response, multipartItemIterator)

        chain.doFilter(CsrfProvidingRequestWrapper(multipartRequest, tokenDetails), response)
    }

    private fun getCsrfTokenDetails(
        multipartRequest: HttpServletRequest,
        response: HttpServletResponse,
        multipartItemIterator: FileItemInputIterator,
    ): ParameterCsrfTokenDetails {
        if (!multipartItemIterator.hasNext()) {
            throw PrsdbWebException("Must have CSRF as first item on forms")
        }

        val csrfParameterName = getCsrfParameterName(multipartRequest, response)

        val csrfItem = multipartItemIterator.next()

        if (!csrfItem.isFormField || csrfItem.fieldName != csrfParameterName) {
            throw PrsdbWebException("Must have CSRF as first item on forms")
        }

        csrfItem.inputStream.use { input ->
            val rawToken = input.reader().readText()
            return ParameterCsrfTokenDetails(csrfParameterName, rawToken)
        }
    }

    private fun getCsrfParameterName(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): String {
        val deferredCsrfToken = tokenRepository.loadDeferredToken(request, response)
        val csrfToken = deferredCsrfToken.get()
        return csrfToken.parameterName
    }

    class CsrfProvidingRequestWrapper(
        private val request: HttpServletRequest,
        private val csrfToken: ParameterCsrfTokenDetails,
    ) : HttpServletRequest by request {
        override fun getParameter(p0: String?): String =
            if (p0 == csrfToken.parameterName) {
                csrfToken.token
            } else {
                request.getParameter(p0)
            }
    }

    class ParameterCsrfTokenDetails(
        val parameterName: String,
        val token: String,
    )

    companion object {
        const val ITERATOR_ATTRIBUTE = "multipartItemIterator"
    }
}
