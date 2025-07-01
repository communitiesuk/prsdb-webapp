package uk.gov.communities.prsdb.webapp.controllers

import kotlinx.datetime.toKotlinInstant
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.UPDATE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordDetailsUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.LandlordViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbController
@RequestMapping
class LandlordDetailsController(
    private val landlordService: LandlordService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val landlordDetailsUpdateJourneyFactory: LandlordDetailsUpdateJourneyFactory,
) {
    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping(LANDLORD_DETAILS_FOR_LANDLORD_ROUTE)
    fun getUserLandlordDetails(
        model: Model,
        principal: Principal,
    ): String {
        val landlord =
            landlordService.retrieveLandlordByBaseUserId(principal.name)
                ?: throw PrsdbWebException("User ${principal.name} is not registered as a landlord")

        val landlordViewModel = LandlordViewModel(landlord, withChangeLinks = true)

        model.addAttribute("name", landlordViewModel.name)
        model.addAttribute("landlord", landlordViewModel)

        val registeredPropertiesList = propertyOwnershipService.getRegisteredPropertiesForLandlordUser(principal.name)

        model.addAttribute("registeredPropertiesList", registeredPropertiesList)
        model.addAttribute("backUrl", LANDLORD_DASHBOARD_URL)
        model.addAttribute("registeredPropertiesTabId", REGISTERED_PROPERTIES_FRAGMENT)

        model.addAttribute("deleteLandlordRecordUrl", DeregisterLandlordController.LANDLORD_DEREGISTRATION_PATH)

        return "landlordDetailsView"
    }

    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("$UPDATE_ROUTE/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        model: Model,
        principal: Principal,
    ): ModelAndView =
        landlordDetailsUpdateJourneyFactory
            .create(principal.name, stepName)
            .getModelAndViewForStep()

    @PreAuthorize("hasRole('LANDLORD')")
    @PostMapping("$UPDATE_ROUTE/{stepName}")
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
        model: Model,
        principal: Principal,
    ): ModelAndView =
        landlordDetailsUpdateJourneyFactory
            .create(principal.name, stepName)
            .completeStep(formData, principal)

    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    @GetMapping(LANDLORD_DETAILS_FOR_LA_USER_ROUTE)
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
        model.addAttribute("registeredPropertiesTabId", REGISTERED_PROPERTIES_FRAGMENT)

        val registeredPropertiesList = propertyOwnershipService.getRegisteredPropertiesForLandlord(id)

        model.addAttribute("registeredPropertiesList", registeredPropertiesList)

        model.addAttribute("backUrl", "/")

        return "localAuthorityLandlordDetailsView"
    }

    companion object {
        const val LANDLORD_DETAILS_FOR_LANDLORD_ROUTE = "/$LANDLORD_PATH_SEGMENT/$LANDLORD_DETAILS_PATH_SEGMENT"
        const val LANDLORD_DETAILS_FOR_LA_USER_ROUTE = "/$LOCAL_AUTHORITY_PATH_SEGMENT/$LANDLORD_DETAILS_PATH_SEGMENT/{id}"
        const val UPDATE_ROUTE = "$LANDLORD_DETAILS_FOR_LANDLORD_ROUTE/$UPDATE_PATH_SEGMENT"

        fun getLandlordDetailsForLaUserPath(landlordId: Long? = null): String =
            UriTemplate(LANDLORD_DETAILS_FOR_LA_USER_ROUTE)
                .expand(landlordId)
                .toASCIIString()
    }
}
