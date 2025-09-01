package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.MAINTENANCE_PATH_SEGMENT

@Profile("maintenance-mode")
@PrsdbController
class MaintenanceController {
    @GetMapping(MAINTENANCE_PATH_SEGMENT)
    fun index(): String = "maintenancePage"

    companion object {
        const val MAINTENANCE_ROUTE = "/$MAINTENANCE_PATH_SEGMENT"
    }
}
