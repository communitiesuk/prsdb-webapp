package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import java.security.Principal
import java.time.LocalDate

@PreAuthorize("hasRole('LANDLORD')")
@Controller
@RequestMapping("/landlord-user")
class LandlordUserController(
    val landlordService: LandlordService,
    val addressDataService: AddressDataService,
) {
    @GetMapping
    fun index(
        model: Model,
        principal: Principal,
    ): String {
        val landlord =
            landlordService.retrieveLandlordByBaseUserId(principal.name)
                ?: throw PrsdbWebException("User ${principal.name} is not registered as a landlord")
        val registeredDate = LocalDate.of(landlord.createdDate.year, landlord.createdDate.monthValue, landlord.createdDate.dayOfMonth)

        // TODO PRSD-747 to pass Id verification status to model with verified label and message key
        model.addAttribute("name", landlord.name)
        model.addAttribute("registrationNumber", RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber))
        model.addAttribute("registrationDate", registeredDate)
        model.addAttribute("dateOfBirth", landlord.dateOfBirth)
        model.addAttribute("email", landlord.email)
        model.addAttribute("phoneNumber", landlord.phoneNumber)
        // TODO PRSD-742 to update this check
        model.addAttribute("isUKResident", MessageKeyConverter.convert(landlord.internationalAddress == null))
        model.addAttribute("internationalAddress", landlord.internationalAddress)
        model.addAttribute("internationalAddress", landlord.internationalAddress)
        model.addAttribute("ukAddress", landlord.address.singleLineAddress)
        // TODO PRSD-670: Replace with link to dashboard
        model.addAttribute("backUrl", "/")

        // TODO

        return "landlordDetailsView"
    }
}
