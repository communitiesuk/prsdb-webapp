package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.WebController

@WebController
@RequestMapping("/healthcheck")
class HealthCheckController {
    @GetMapping
    fun healthCheck(response: HttpServletResponse) {
        response.status = HttpServletResponse.SC_OK
    }
}
