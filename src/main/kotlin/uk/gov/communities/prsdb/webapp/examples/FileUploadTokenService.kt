package uk.gov.communities.prsdb.webapp.examples

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Service
import java.util.UUID

// TODO: PRSD-945/PRSD-395 Move this to service folder and manage multiple tokens
@Service
class FileUploadTokenService(
    private val session: HttpSession,
) {
    fun issueToken(): String {
        val token = UUID.randomUUID().toString()
        session.setAttribute(token, true)
        return token
    }

    fun checkToken(token: String): Boolean {
        if (session.getAttribute(token) == true) {
            session.removeAttribute(token)
            return true
        } else {
            return false
        }
    }
}
