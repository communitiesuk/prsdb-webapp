package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import java.security.Principal
import java.time.LocalDate

@Controller
@RequestMapping("/landlord-details")
class LandlordDetailsController(
    val landlordService: LandlordService,
    val addressDataService: AddressDataService,
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
        val registeredDate =
            LocalDate.of(landlord.createdDate.year, landlord.createdDate.monthValue, landlord.createdDate.dayOfMonth)

        // Add personal details to model
        // TODO PRSD-747 to pass Id verification status to model with verified label and message key
        model.addAttribute("name", landlord.name)
        model.addAttribute("registrationDate", registeredDate)
        model.addAttribute("dateOfBirth", landlord.dateOfBirth)
        model.addAttribute("email", landlord.email)
        model.addAttribute("phoneNumber", landlord.phoneNumber)
        model.addAttribute("isUKResident", MessageKeyConverter.convert(landlord.internationalAddress == null))
        // TODO PRSD-742 will make country of residence available and should be displayed alongside the international address
        model.addAttribute("internationalAddress", landlord.internationalAddress)
        model.addAttribute("ukAddress", landlord.address.singleLineAddress)
        // TODO PRSD-670: Replace with link to dashboard
        model.addAttribute("backUrl", "/")

        // TODO PRSD-746 - add user consent information to this page once it is captured.

        // Add properties to model
        // TODO PRSD-702 add properties to model

        return "landlordDetailsView"
    }

    // TODO PRSD-656: return LA view of landlord details
    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    @GetMapping("/{id}")
    fun getLandlordDetails(
        @PathVariable id: String,
    ) = "error/404"
}
