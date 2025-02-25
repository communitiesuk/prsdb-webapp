package uk.gov.communities.prsdb.webapp.controllers

import kotlinx.datetime.toKotlinInstant
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsLandlordViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@Controller
@RequestMapping
class PropertyDetailsController(
    val propertyOwnershipService: PropertyOwnershipService,
) {
    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping(PROPERTY_DETAILS_ROUTE)
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
                landlordDetailsUrl = LandlordDetailsController.LANDLORD_DETAILS_ROUTE,
            )

        val landlordViewModel =
            PropertyDetailsLandlordViewModel(
                landlord = propertyOwnership.primaryLandlord,
                withChangeLinks = true,
                landlordDetailsUrl = LandlordDetailsController.LANDLORD_DETAILS_ROUTE,
            )

        model.addAttribute("propertyDetails", propertyDetails)
        model.addAttribute("landlordDetails", landlordViewModel.landlordsDetails)
        model.addAttribute("deleteRecordLink", "delete-record")
        // TODO PRSD-670: Replace with link to dashboard
        model.addAttribute("backUrl", "/")

        return "propertyDetailsView"
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
                landlord = propertyOwnership.primaryLandlord,
                withChangeLinks = false,
                landlordDetailsUrl = "${LandlordDetailsController.LANDLORD_DETAILS_ROUTE}/${propertyOwnership.primaryLandlord.id}",
            )

        model.addAttribute("propertyDetails", propertyDetails)
        model.addAttribute("lastModifiedDate", lastModifiedDate)
        model.addAttribute("lastModifiedBy", lastModifiedBy)
        model.addAttribute("landlordDetails", landlordViewModel.landlordsDetails)
        model.addAttribute("backUrl", LOCAL_AUTHORITY_DASHBOARD_URL)

        return "propertyDetailsView"
    }

    companion object {
        const val PROPERTY_DETAILS_ROUTE = "/property-details/{propertyOwnershipId}"

        const val LA_PROPERTY_DETAILS_ROUTE = "/local-authority$PROPERTY_DETAILS_ROUTE"

        fun getPropertyDetailsPath(
            propertyOwnershipId: Long,
            isLaView: Boolean = false,
        ): String =
            UriTemplate(if (isLaView) LA_PROPERTY_DETAILS_ROUTE else PROPERTY_DETAILS_ROUTE)
                .expand(propertyOwnershipId)
                .toASCIIString()
    }
}
