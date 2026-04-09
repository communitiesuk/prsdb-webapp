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
        var wrappedResponseOrNull: CSPNonceResponseWrapper? = null

        if (response != null) {
            val nonce = getOrGenerateNonce(request)
            wrappedResponseOrNull = CSPNonceResponseWrapper(response, nonce)
        }

        chain.doFilter(request, wrappedResponseOrNull)
    }

    private fun getOrGenerateNonce(request: HttpServletRequest?): String {
        var nonce = getNonceFromRequestOrNull(request)
        if (nonce == null) {
            nonce = generateNonce()
            setCSPNonceAttribute(request, nonce)
        }
        return nonce
    }

    private fun getNonceFromRequestOrNull(request: HttpServletRequest?): String? {
        return request?.getAttribute(CSP_NONCE_ATTRIBUTE) as String?
    }

    private fun setCSPNonceAttribute(
        request: HttpServletRequest?,
        nonce: String,
    ) {
        request?.setAttribute(CSP_NONCE_ATTRIBUTE, nonce)
    }

    private fun generateNonce(): String {
        val secureRandom = java.security.SecureRandom()
        val nonceArray = ByteArray(NONCE_SIZE)
        secureRandom.nextBytes(nonceArray)
        return Base64.getEncoder().encodeToString(nonceArray)
    }

    class CSPNonceResponseWrapper(
        response: HttpServletResponse,
        private val nonce: String,
    ) : HttpServletResponseWrapper(response) {
        override fun setHeader(
            name: String,
            value: String?,
        ) {
            if ((name == "Content-Security-Policy") && !value.isNullOrBlank()) {
                val newValue = value.replace("'nonce-'", "'nonce-$nonce'")
                super.setHeader(name, newValue)
            } else {
                super.setHeader(name, value)
            }
        }

        override fun addHeader(
            name: String,
            value: String?,
        ) {
            if ((name == "Content-Security-Policy") && !value.isNullOrBlank()) {
                val newValue = value.replace("'nonce-'", "'nonce-$nonce'")
                super.addHeader(name, newValue)
            } else {
                super.addHeader(name, value)
            }
        }
    }

    companion object {
        const val NONCE_SIZE = 32
        const val CSP_NONCE_ATTRIBUTE = "CSP_NONCE_ATTRIBUTE"
    }
}
