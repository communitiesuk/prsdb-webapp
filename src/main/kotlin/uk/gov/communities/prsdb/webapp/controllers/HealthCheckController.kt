package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.HEALTHCHECK_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.HealthCheckController.Companion.HEALTHCHECK_ROUTE

@PrsdbController
@RequestMapping(HEALTHCHECK_ROUTE)
class HealthCheckController {
    @GetMapping
    fun healthCheck(response: HttpServletResponse) {
        response.status = HttpServletResponse.SC_OK
    }

    companion object {
        const val HEALTHCHECK_ROUTE = "/$HEALTHCHECK_PATH_SEGMENT"
    }
}
