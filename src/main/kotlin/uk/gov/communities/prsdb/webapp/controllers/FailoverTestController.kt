package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController

@PrsdbController
@RequestMapping(LandlordController.LANDLORD_BASE_URL)
class FailoverTestController {
    @GetMapping("/failover-502")
    fun failover502(response: HttpServletResponse): Unit =
        throw ResponseStatusException(
            HttpStatus.BAD_GATEWAY,
            "502 error",
        )

    @GetMapping("/failover-503")
    fun failover503(response: HttpServletResponse): Unit =
        throw ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE,
            "503 error",
        )

    @GetMapping("/failover-504")
    fun failover504(response: HttpServletResponse): Unit =
        throw ResponseStatusException(
            HttpStatus.GATEWAY_TIMEOUT,
            "504 error",
        )
}
