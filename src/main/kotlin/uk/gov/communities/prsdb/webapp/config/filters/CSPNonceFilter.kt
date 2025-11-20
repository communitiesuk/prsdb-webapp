package uk.gov.communities.prsdb.webapp.config.filters

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponseWrapper
import java.util.Base64

class CSPNonceFilter() : Filter {
    override fun doFilter(
        request: ServletRequest?,
        response: ServletResponse?,
        chain: FilterChain,
    ) {
        doFilter(request as HttpServletRequest, response as HttpServletResponse, chain)
    }

    private fun doFilter(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        chain: FilterChain,
    ) {
        if (response != null) {
            val secureRandom = java.security.SecureRandom()
//    val secureRandom = random() // This kotlin method uses the Java one above?
            val nonceArray = ByteArray(NONCE_SIZE)
            secureRandom.nextBytes(nonceArray)
            val nonce = Base64.getEncoder().encodeToString(nonceArray)

            request?.setAttribute(CSP_NONCE_ATTRIBUTE, nonce)

            try {
                chain.doFilter(request, CSPNonceResponseWrapper(response, nonce))
            } catch (e: Exception) {
                throw e
            }
        } else {
            try {
                chain.doFilter(request, null)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    class CSPNonceResponseWrapper(
        response: HttpServletResponse,
        private val nonce: String,
    ) : HttpServletResponseWrapper(response) {
        override fun setHeader(
            name: String,
            value: String,
        ) {
            if ((name == "Content-Security-Policy") && (value.isNotBlank())) {
                val newValue = value.replace("'nonce-'", "'nonce-$nonce'")
                (this as HttpServletResponse).setHeader(name, newValue)
            } else {
                (this as HttpServletResponse).setHeader(name, value)
            }
        }

        override fun addHeader(
            name: String,
            value: String,
        ) {
            if ((name == "Content-Security-Policy") && (value.isNotBlank())) {
                val newValue = value.replace("'nonce-'", "'nonce-$nonce'")
                (this as HttpServletResponse).addHeader(name, newValue)
            } else {
                (this as HttpServletResponse).addHeader(name, value)
            }
        }
    }

    companion object {
        const val NONCE_SIZE = 32
        const val CSP_NONCE_ATTRIBUTE = "CSP_NONCE_ATTRIBUTE"
    }
}
