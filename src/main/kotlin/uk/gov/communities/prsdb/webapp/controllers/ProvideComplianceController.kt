package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_COMPLIANCE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ProvideComplianceController.Companion.PROVIDE_COMPLIANCE_ROUTE
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@Controller
@PreAuthorize("hasRole('LANDLORD')")
@RequestMapping(PROVIDE_COMPLIANCE_ROUTE)
class ProvideComplianceController(
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    @GetMapping
    fun index(
        model: Model,
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
    ): String {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        // TODO PRSD:941: Add link to task list
        model.addAttribute("taskListUrl", "#")
        return "provideComplianceStartPage"
    }

    private fun throwErrorIfUserIsNotAuthorized(
        baseUserId: String,
        propertyOwnershipId: Long,
    ) {
        if (!propertyOwnershipService.getIsPrimaryLandlord(propertyOwnershipId, baseUserId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User $baseUserId is not authorized to provide compliance for property ownership $propertyOwnershipId",
            )
        }
    }

    companion object {
        const val PROVIDE_COMPLIANCE_ROUTE = "/$PROVIDE_COMPLIANCE_PATH_SEGMENT/{propertyOwnershipId}"

        fun getProvideCompliancePath(propertyOwnershipId: Long): String =
            UriTemplate(PROVIDE_COMPLIANCE_ROUTE).expand(propertyOwnershipId).toASCIIString()
    }
}
