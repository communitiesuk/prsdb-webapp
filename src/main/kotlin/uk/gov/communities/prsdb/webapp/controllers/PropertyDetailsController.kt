package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@Controller
@RequestMapping("/property-details")
class PropertyDetailsController(
    val propertyOwnershipService: PropertyOwnershipService,
) {
    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("/{propertyOwnershipId}")
    fun getPropertyDetails(
        @PathVariable propertyOwnershipId: Long,
        model: Model,
    ): String {
        val propertyOwnership = propertyOwnershipService.retrievePropertyOwnership(propertyOwnershipId)

        return "propertyDetailsView"
    }
}
