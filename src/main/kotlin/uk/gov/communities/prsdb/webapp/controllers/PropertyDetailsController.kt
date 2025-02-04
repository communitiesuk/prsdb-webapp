package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.models.viewModels.PropertyDetailsViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@Controller
@RequestMapping
class PropertyDetailsController(
    val propertyOwnershipService: PropertyOwnershipService,
) {
    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("/property-details/{propertyOwnershipId}")
    fun getPropertyDetails(
        @PathVariable propertyOwnershipId: Long,
        model: Model,
    ): String {
        val propertyOwnership =
            propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Property ownership $propertyOwnershipId not found")
        if (!propertyOwnership.isActive) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Property ownership $propertyOwnershipId is inactive")
        }

        val propertyDetails =
            PropertyDetailsViewModel(
                propertyOwnership = propertyOwnership,
                withChangeLinks = true,
                hideNullUprn = true,
                landlordDetailsUrl = "/landlord-details",
            )

        model.addAttribute("propertyDetails", propertyDetails)
        model.addAttribute("deleteRecordLink", "delete-record")

        return "propertyDetailsView"
    }

    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    @GetMapping("/view-property-details/{propertyOwnershipId}")
    fun getPropertyDetailsLaView(
        @PathVariable propertyOwnershipId: Long,
        model: Model,
    ): String {
        val propertyOwnership =
            propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Property ownership $propertyOwnershipId not found")
        if (!propertyOwnership.isActive) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Property ownership $propertyOwnershipId is inactive")
        }

        val propertyDetails =
            PropertyDetailsViewModel(
                propertyOwnership = propertyOwnership,
                withChangeLinks = false,
                hideNullUprn = false,
                landlordDetailsUrl = "/landlord-details/${propertyOwnership.primaryLandlord.id}",
            )

        model.addAttribute("propertyDetails", propertyDetails)

        // TODO PRSD-647: Replace with link to dashboard
        model.addAttribute("backUrl", "/")

        return "propertyDetailsView"
    }
}
