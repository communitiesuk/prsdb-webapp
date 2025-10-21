package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.MAINTENANCE_PATH_SEGMENT

// @Profile("maintenance-mode")
@PrsdbController
class MaintenanceController {
    @GetMapping(MAINTENANCE_PATH_SEGMENT)
    fun index(): String =
        throw ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE,
            "503 error",
        )

    companion object {
        const val MAINTENANCE_ROUTE = "/$MAINTENANCE_PATH_SEGMENT"
    }
}
