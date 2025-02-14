package uk.gov.communities.prsdb.webapp.controllers

import kotlinx.datetime.toKotlinInstant
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.UpdateLandlordDetailsJourney
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
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
    val updateDetailsJourney: UpdateLandlordDetailsJourney,
) {
    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("update/details")
    fun getUpdateUserLandlordDetails(
        model: Model,
        principal: Principal,
    ): String {
        updateDetailsJourney.initialiseJourneyDataIfNotInitialised(principal.name)
        return getLandlordDetailsPage(model, principal, includeChangeLinks = true)
    }

    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping
    fun getUserLandlordDetails(
        model: Model,
        principal: Principal,
    ): String = getLandlordDetailsPage(model, principal, includeChangeLinks = false)

    private fun getLandlordDetailsPage(
        model: Model,
        principal: Principal,
        includeChangeLinks: Boolean,
    ): String {
        val landlord =
            landlordService.retrieveLandlordByBaseUserId(principal.name)
                ?: throw PrsdbWebException("User ${principal.name} is not registered as a landlord")

        val landlordViewModel = LandlordViewModel(landlord, includeChangeLinks)

        model.addAttribute("name", landlordViewModel.name)
        model.addAttribute("landlord", landlordViewModel)

        val registeredPropertiesList = propertyOwnershipService.getRegisteredPropertiesForLandlord(principal.name)

        model.addAttribute("registeredPropertiesList", registeredPropertiesList)

        // TODO PRSD-670: Replace with link to dashboard
        model.addAttribute("backUrl", "/")

        return "landlordDetailsView"
    }

    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("update/{stepName}")
    fun getUpdateEmail(
        @PathVariable("stepName") stepName: String,
        model: Model,
        principal: Principal,
    ): String =
        updateDetailsJourney.populateModelAndGetViewName(
            updateDetailsJourney.getStepId(stepName),
            model,
            null,
        )

    @PreAuthorize("hasRole('LANDLORD')")
    @PostMapping("update/{stepName}")
    fun submitUpdateEmail(
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
        model: Model,
        principal: Principal,
    ): String =
        updateDetailsJourney.updateJourneyDataAndGetViewNameOrRedirect(
            updateDetailsJourney.getStepId(stepName),
            formData,
            model,
            null,
            principal,
        )

    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    @GetMapping("/{id}")
    fun getLandlordDetails(
        @PathVariable id: Long,
        model: Model,
    ): String {
        val landlord =
            landlordService.retrieveLandlordById(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Landlord $id not found")

        val lastModifiedDate = DateTimeHelper.getDateInUK(landlord.getMostRecentlyUpdated().toKotlinInstant())

        val landlordViewModel = LandlordViewModel(landlord = landlord, withChangeLinks = false)

        model.addAttribute("name", landlordViewModel.name)
        model.addAttribute("lastModifiedDate", lastModifiedDate)
        model.addAttribute("landlord", landlordViewModel)

        val registeredPropertiesList = propertyOwnershipService.getRegisteredPropertiesForLandlord(id)

        model.addAttribute("registeredPropertiesList", registeredPropertiesList)

        // TODO PRSD-805: Replace with previous url for back link
        model.addAttribute("backUrl", "/")

        return "localAuthorityLandlordDetailsView"
    }

    companion object {
        const val UPDATE_ROUTE = "/landlord-details/update"
    }
}
