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
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.UPDATE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController.Companion.getLandlordDeregistrationPath
import uk.gov.communities.prsdb.webapp.controllers.LandlordDashboardController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordDetailsUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateLandlordDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.LandlordViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@Controller
@RequestMapping(LandlordDetailsController.LANDLORD_DETAILS_ROUTE)
class LandlordDetailsController(
    private val landlordService: LandlordService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val landlordDetailsUpdateJourneyFactory: LandlordDetailsUpdateJourneyFactory,
) {
    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("$UPDATE_PATH_SEGMENT/$DETAILS_PATH_SEGMENT")
    fun getUpdateUserLandlordDetails(
        model: Model,
        principal: Principal,
    ): ModelAndView {
        addLandlordDetailsToModel(model, principal, includeChangeLinks = true)
        // TODO: PRSD-355 Remove this way of showing submit button
        model.addAttribute("shouldShowSubmitButton", true)
        return landlordDetailsUpdateJourneyFactory
            .create(principal.name)
            .getModelAndViewForStep(UpdateLandlordDetailsStepId.UpdateDetails.urlPathSegment, subPageNumber = null)
    }

    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping
    fun getUserLandlordDetails(
        model: Model,
        principal: Principal,
    ): String {
        addLandlordDetailsToModel(model, principal, includeChangeLinks = false)

        return "landlordDetailsView"
    }

    private fun addLandlordDetailsToModel(
        model: Model,
        principal: Principal,
        includeChangeLinks: Boolean,
    ) {
        val landlord =
            landlordService.retrieveLandlordByBaseUserId(principal.name)
                ?: throw PrsdbWebException("User ${principal.name} is not registered as a landlord")

        val landlordViewModel = LandlordViewModel(landlord, includeChangeLinks)

        model.addAttribute("name", landlordViewModel.name)
        model.addAttribute("landlord", landlordViewModel)

        val registeredPropertiesList = propertyOwnershipService.getRegisteredPropertiesForLandlordUser(principal.name)

        model.addAttribute("registeredPropertiesList", registeredPropertiesList)
        model.addAttribute("backUrl", LANDLORD_DASHBOARD_URL)
        model.addAttribute("registeredPropertiesTabId", REGISTERED_PROPERTIES_PATH_SEGMENT)

        model.addAttribute("deleteLandlordRecordUrl", DeregisterLandlordController.getLandlordDeregistrationPath())
    }

    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("${UPDATE_PATH_SEGMENT}/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        model: Model,
        principal: Principal,
    ): ModelAndView =
        landlordDetailsUpdateJourneyFactory
            .create(principal.name)
            .getModelAndViewForStep(stepName, subPageNumber = null)

    @PreAuthorize("hasRole('LANDLORD')")
    @PostMapping("${UPDATE_PATH_SEGMENT}/{stepName}")
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
        model: Model,
        principal: Principal,
    ): ModelAndView =
        landlordDetailsUpdateJourneyFactory
            .create(principal.name)
            .completeStep(
                stepName,
                formData,
                subPageNumber = null,
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
        model.addAttribute("registeredPropertiesTabId", REGISTERED_PROPERTIES_PATH_SEGMENT)

        val registeredPropertiesList = propertyOwnershipService.getRegisteredPropertiesForLandlord(id)

        model.addAttribute("registeredPropertiesList", registeredPropertiesList)

        // TODO PRSD-805: Replace with previous url for back link
        model.addAttribute("backUrl", "/")

        return "localAuthorityLandlordDetailsView"
    }

    companion object {
        const val LANDLORD_DETAILS_ROUTE = "/$LANDLORD_DETAILS_PATH_SEGMENT"
        const val UPDATE_ROUTE = "$LANDLORD_DETAILS_ROUTE/$UPDATE_PATH_SEGMENT"
    }
}
