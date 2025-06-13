package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpSession
import uk.gov.communities.prsdb.webapp.annotations.WebService
import uk.gov.communities.prsdb.webapp.constants.COOKIE_TOKENS
import java.util.UUID

@WebService
class TokenCookieService(
    private val session: HttpSession,
) {
    fun createCookieForValue(
        name: String,
        path: String,
        value: Any = path,
    ): Cookie {
        val token = issueTokenForCookieValue(value)
        return Cookie(name, token).apply {
            this.path = path
            this.isHttpOnly = true
            this.secure = true
        }
    }

    fun isTokenForCookieValue(
        token: String,
        cookieValue: Any,
    ) = getTokensFromSession()[token] == cookieValue

    fun useToken(token: String) = removeTokenFromSession(token)

    private fun issueTokenForCookieValue(value: Any): String {
        val token = UUID.randomUUID().toString()
        addTokenToSession(token, value)
        return token
    }

    private fun addTokenToSession(
        token: String,
        value: Any,
    ) {
        val updatedTokens = getTokensFromSession() + (token to value)
        session.setAttribute(COOKIE_TOKENS, updatedTokens)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getTokensFromSession() = session.getAttribute(COOKIE_TOKENS) as? Map<String, Any> ?: emptyMap()

    private fun removeTokenFromSession(token: String) {
        val updatedTokens = getTokensFromSession() - token
        session.setAttribute(COOKIE_TOKENS, updatedTokens)
    }
}
