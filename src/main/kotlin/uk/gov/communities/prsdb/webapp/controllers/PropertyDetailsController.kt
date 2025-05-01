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
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.UPDATE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordDashboardController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDetailsUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsLandlordViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@Controller
@RequestMapping
class PropertyDetailsController(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyDetailsUpdateJourneyFactory: PropertyDetailsUpdateJourneyFactory,
) {
    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping(PROPERTY_DETAILS_ROUTE)
    fun getPropertyDetails(
        @PathVariable propertyOwnershipId: Long,
        model: Model,
        principal: Principal,
    ): String {
        addPropertyDetailsToModelIfAuthorizedUser(model, principal, propertyOwnershipId)
        return "propertyDetailsView"
    }

    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("$UPDATE_PROPERTY_DETAILS_ROUTE/$DETAILS_PATH_SEGMENT")
    fun getUpdatePropertyDetails(
        model: Model,
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
    ): String {
        addPropertyDetailsToModelIfAuthorizedUser(model, principal, propertyOwnershipId, withPropertyChangeLinks = true)
        // TODO: PRSD-355 Remove this way of showing submit button
        model.addAttribute("shouldShowSubmitButton", true)
        model.addAttribute(BACK_URL_ATTR_NAME, PROPERTY_DETAILS_ROUTE)

        return "propertyDetailsView"
    }

    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("$UPDATE_PROPERTY_DETAILS_ROUTE/{stepName}")
    fun getJourneyStep(
        model: Model,
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
    ): ModelAndView =
        if (propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, principal.name)) {
            propertyDetailsUpdateJourneyFactory
                .create(propertyOwnershipId, stepName)
                .getModelAndViewForStep()
        } else {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Base user ${principal.name} is not the primary landlord of property ownership $propertyOwnershipId",
            )
        }

    @PreAuthorize("hasRole('LANDLORD')")
    @PostMapping("$UPDATE_PROPERTY_DETAILS_ROUTE/{stepName}")
    fun postJourneyData(
        model: Model,
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
    ): ModelAndView =
        if (propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, principal.name)) {
            propertyDetailsUpdateJourneyFactory
                .create(propertyOwnershipId, stepName)
                .completeStep(formData, principal)
        } else {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Base user ${principal.name} is not the primary landlord of property ownership $propertyOwnershipId",
            )
        }

    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    @GetMapping(LA_PROPERTY_DETAILS_ROUTE)
    fun getPropertyDetailsLaView(
        @PathVariable propertyOwnershipId: Long,
        model: Model,
        principal: Principal,
    ): String {
        val propertyOwnership =
            propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(propertyOwnershipId, principal.name)

        val lastModifiedDate = DateTimeHelper.getDateInUK(propertyOwnership.getMostRecentlyUpdated().toKotlinInstant())
        val lastModifiedBy = propertyOwnership.primaryLandlord.name

        val propertyDetails =
            PropertyDetailsViewModel(
                propertyOwnership = propertyOwnership,
                withChangeLinks = false,
                hideNullUprn = false,
                landlordDetailsUrl = "${LandlordDetailsController.LANDLORD_DETAILS_ROUTE}/${propertyOwnership.primaryLandlord.id}",
            )

        val landlordViewModel =
            PropertyDetailsLandlordViewModel(
                propertyOwnership.primaryLandlord,
                "${LandlordDetailsController.LANDLORD_DETAILS_ROUTE}/${propertyOwnership.primaryLandlord.id}",
            )

        model.addAttribute("propertyDetails", propertyDetails)
        model.addAttribute("lastModifiedDate", lastModifiedDate)
        model.addAttribute("lastModifiedBy", lastModifiedBy)
        model.addAttribute("landlordDetails", landlordViewModel.landlordsDetails)
        model.addAttribute("backUrl", LOCAL_AUTHORITY_DASHBOARD_URL)

        return "propertyDetailsView"
    }

    private fun addPropertyDetailsToModelIfAuthorizedUser(
        model: Model,
        principal: Principal,
        propertyOwnershipId: Long,
        withPropertyChangeLinks: Boolean = false,
    ) {
        val propertyOwnership =
            propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(propertyOwnershipId, principal.name)

        val propertyDetails =
            PropertyDetailsViewModel(
                propertyOwnership = propertyOwnership,
                withChangeLinks = withPropertyChangeLinks,
                hideNullUprn = true,
                landlordDetailsUrl = LandlordDetailsController.LANDLORD_DETAILS_ROUTE,
            )

        val landlordViewModel =
            PropertyDetailsLandlordViewModel(
                landlord = propertyOwnership.primaryLandlord,
                landlordDetailsUrl = LandlordDetailsController.LANDLORD_DETAILS_ROUTE,
            )

        model.addAttribute("propertyDetails", propertyDetails)
        model.addAttribute("landlordDetails", landlordViewModel.landlordsDetails)
        model.addAttribute("deleteRecordLink", DeregisterPropertyController.getPropertyDeregistrationPath(propertyOwnershipId))
        model.addAttribute("backUrl", LANDLORD_DASHBOARD_URL)
    }

    companion object {
        const val PROPERTY_DETAILS_ROUTE = "/$PROPERTY_DETAILS_SEGMENT/{propertyOwnershipId}"

        const val UPDATE_PROPERTY_DETAILS_ROUTE = "$PROPERTY_DETAILS_ROUTE/$UPDATE_PATH_SEGMENT"

        const val LA_PROPERTY_DETAILS_ROUTE = "/$LOCAL_AUTHORITY_PATH_SEGMENT$PROPERTY_DETAILS_ROUTE"

        fun getPropertyDetailsPath(
            propertyOwnershipId: Long,
            isLaView: Boolean = false,
        ): String =
            UriTemplate(if (isLaView) LA_PROPERTY_DETAILS_ROUTE else PROPERTY_DETAILS_ROUTE)
                .expand(propertyOwnershipId)
                .toASCIIString()

        fun getUpdatePropertyDetailsPath(propertyOwnershipId: Long): String =
            UriTemplate(UPDATE_PROPERTY_DETAILS_ROUTE).expand(propertyOwnershipId).toASCIIString()
    }
}
