package uk.gov.communities.prsdb.webapp.examples

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Service
import java.util.UUID

// TODO: PRSD-945/PRSD-395 Move this to service folder and manage multiple tokens
// It may be that these session attributes should/will need store additional data, e.g. property ownership id
@Service
class FileUploadTokenService(
    private val session: HttpSession,
) {
    fun issueTokenFor(value: Any): String {
        val token = UUID.randomUUID().toString()
        session.setAttribute(token, value)
        return token
    }

    fun checkTokenIsFor(
        token: String,
        value: Any,
    ): Boolean {
        if (session.getAttribute(token) == value) {
            session.removeAttribute(token)
            return true
        } else {
            return false
        }
    }
}
