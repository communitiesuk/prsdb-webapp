package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.viewModels.LandlordViewModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@Controller
@RequestMapping("/landlord-details")
class LandlordDetailsController(
    val landlordService: LandlordService,
    val addressDataService: AddressDataService,
    val propertyOwnershipService: PropertyOwnershipService,
) {
    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping
    fun getUserLandlordDetails(
        model: Model,
        principal: Principal,
    ): String {
        val landlord =
            landlordService.retrieveLandlordByBaseUserId(principal.name)
                ?: throw PrsdbWebException("User ${principal.name} is not registered as a landlord")

        val landlordViewModel = LandlordViewModel(landlord)

        model.addAttribute("name", landlordViewModel.name)
        model.addAttribute("personalDetails", landlordViewModel.personalDetails)
        model.addAttribute("consentInformation", landlordViewModel.consentInformation)

        val registeredPropertiesList = propertyOwnershipService.getRegisteredPropertiesForLandlord(principal.name)

        model.addAttribute("registeredPropertiesList", registeredPropertiesList)

        // TODO PRSD-670: Replace with link to dashboard
        model.addAttribute("backUrl", "/")

        return "landlordDetailsView"
    }

    // TODO PRSD-656: return LA view of landlord details
    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    @GetMapping("/{id}")
    fun getLandlordDetails(
        @PathVariable id: String,
    ) = "error/404"

    companion object {
        const val UPDATE_ROUTE = "landlord-details/update"
    }
}
