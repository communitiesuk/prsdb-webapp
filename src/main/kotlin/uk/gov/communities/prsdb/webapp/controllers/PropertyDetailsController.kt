package uk.gov.communities.prsdb.webapp.controllers

import kotlinx.datetime.toKotlinInstant
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.PropertyDetailsLandlordViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.PropertyDetailsViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@Controller
@RequestMapping
class PropertyDetailsController(
    val propertyOwnershipService: PropertyOwnershipService,
    val landlordService: LandlordService,
) {
    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("/property-details/{propertyOwnershipId}")
    fun getPropertyDetails(
        @PathVariable propertyOwnershipId: Long,
        model: Model,
        principal: Principal,
    ): String {
        val propertyOwnership =
            propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(propertyOwnershipId, principal.name)

        val propertyDetails =
            PropertyDetailsViewModel(
                propertyOwnership = propertyOwnership,
                withChangeLinks = true,
                hideNullUprn = true,
                landlordDetailsUrl = "/landlord-details",
            )

        val landlordViewModel = PropertyDetailsLandlordViewModel(landlord = propertyOwnership.primaryLandlord)

        model.addAttribute("propertyDetails", propertyDetails)
        model.addAttribute("landlordDetails", landlordViewModel.landlordsDetails)
        model.addAttribute("deleteRecordLink", "delete-record")
        // TODO PRSD-647: Replace with link to dashboard
        model.addAttribute("backUrl", "/")

        return "propertyDetailsView"
    }

    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    @GetMapping("local-authority/property-details/{propertyOwnershipId}")
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

        val lastModifiedDate = DateTimeHelper.getDateInUK(propertyOwnership.lastModifiedDate.toKotlinInstant())
        val lastModifiedBy = propertyOwnership.primaryLandlord.name

        val propertyDetails =
            PropertyDetailsViewModel(
                propertyOwnership = propertyOwnership,
                withChangeLinks = false,
                hideNullUprn = false,
                landlordDetailsUrl = "/landlord-details/${propertyOwnership.primaryLandlord.id}",
            )

        model.addAttribute("propertyDetails", propertyDetails)
        model.addAttribute("lastModifiedDate", lastModifiedDate)
        model.addAttribute("lastModifiedBy", lastModifiedBy)

        // TODO PRSD-647: Replace with link to dashboard
        model.addAttribute("backUrl", "/")

        return "propertyDetailsView"
    }
}
