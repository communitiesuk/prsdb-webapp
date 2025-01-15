package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.viewModels.SummaryListRowViewModel
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

        model.addAttribute("personalDetails", formatLandlordPersonalDetails(landlord))
        model.addAttribute("consentInformation", getConsentInformation(landlord))

        // TODO PRSD-670: Replace with link to dashboard
        model.addAttribute("backUrl", "/")

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

    private fun formatLandlordPersonalDetails(landlord: Landlord): List<SummaryListRowViewModel> {
        val registeredDate = LocalDate.of(landlord.createdDate.year, landlord.createdDate.monthValue, landlord.createdDate.dayOfMonth)
        val isUkResident = landlord.internationalAddress == null

        val residencyIndependentPersonalDetails =
            listOf(
                SummaryListRowViewModel("landlordDetails.personalDetails.registrationDate", registeredDate, null),
                // TODO PRSD-747 to pass Id verification status to model with verified label and message key
                SummaryListRowViewModel("landlordDetails.personalDetails.name", landlord.name, null),
                SummaryListRowViewModel("landlordDetails.personalDetails.dateOfBirth", landlord.dateOfBirth, null),
                SummaryListRowViewModel("landlordDetails.personalDetails.emailAddress", landlord.email, null),
                SummaryListRowViewModel("landlordDetails.personalDetails.telephoneNumber", landlord.phoneNumber, null),
                SummaryListRowViewModel("landlordDetails.personalDetails.ukResident", isUkResident, null),
            )

        val residencyPersonalDetails =
            if (isUkResident) {
                formatUkAddressDetails(landlord)
            } else {
                formatNonUkAddressDetails(landlord)
            }

        return residencyIndependentPersonalDetails + residencyPersonalDetails
    }

    private fun formatNonUkAddressDetails(landlord: Landlord) =
        listOf(
            // TODO: PRSD-742 to add country as a separate field
            SummaryListRowViewModel("landlordDetails.personalDetails.country", "TODO: PRSD-742", null),
            SummaryListRowViewModel("landlordDetails.personalDetails.nonUkAddress", landlord.internationalAddress, null),
            SummaryListRowViewModel("landlordDetails.personalDetails.ukAddress", landlord.address.singleLineAddress, null),
        )

    private fun formatUkAddressDetails(landlord: Landlord) =
        listOf(
            SummaryListRowViewModel(
                "landlordDetails.personalDetails.contactAddress",
                landlord.address.singleLineAddress,
                null,
            ),
        )

    private fun getConsentInformation(landlord: Landlord): List<SummaryListRowViewModel> {
        // TODO PRSD-746 - add user consent information to this page once it is captured
        return listOf(
            SummaryListRowViewModel("landlordDetails.personalDetails.optionalChoices.legalChanges", "TODO PRSD-746", null),
            SummaryListRowViewModel("landlordDetails.personalDetails.optionalChoices.research", "TODO PRSD-746", null),
        )
    }
}
