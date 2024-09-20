package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Service

@Service
class ExampleCountingService(
    var session: HttpSession,
) {
    fun getCountAndIncrement(): Int {
        var count = session.getAttribute("VISIT_COUNT")?.toString()?.toInt()
        if (count == null) {
            count = 1
        } else {
            count += 1
        }
        session.setAttribute("VISIT_COUNT", count)
        return count
    }
}
