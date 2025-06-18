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
import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.constants.CHANGE_ANSWER_FOR_PARAMETER_NAME
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.STARTING_UPDATE_PARAMETER_NAME
import uk.gov.communities.prsdb.webapp.constants.UPDATE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDetailsUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsLandlordViewModelBuilder
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsViewModel
import uk.gov.communities.prsdb.webapp.services.BackUrlStorageService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import java.security.Principal

@PrsdbController
@RequestMapping
class PropertyDetailsController(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyDetailsUpdateJourneyFactory: PropertyDetailsUpdateJourneyFactory,
    private val backLinkStorageService: BackUrlStorageService,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
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

        val landlordDetailsUrl =
            LandlordDetailsController
                .getLandlordDetailsPath()
                .overrideBackLinkForUrl(backLinkStorageService.storeCurrentUrlReturningKey())

        val propertyDetails =
            PropertyDetailsViewModel(
                propertyOwnership = propertyOwnership,
                withChangeLinks = true,
                hideNullUprn = true,
                landlordDetailsUrl = landlordDetailsUrl,
            )

        val landlordViewModel =
            PropertyDetailsLandlordViewModelBuilder.fromEntity(
                propertyOwnership.primaryLandlord,
                landlordDetailsUrl,
            )

        model.addAttribute("propertyDetails", propertyDetails)
        model.addAttribute("landlordDetails", landlordViewModel)
        model.addAttribute("deleteRecordLink", DeregisterPropertyController.getPropertyDeregistrationPath(propertyOwnershipId))
        model.addAttribute("backUrl", LANDLORD_DASHBOARD_URL)

        return "propertyDetailsView"
    }

    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("$UPDATE_PROPERTY_DETAILS_ROUTE/{stepName}")
    fun getJourneyStep(
        model: Model,
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam(CHANGE_ANSWER_FOR_PARAMETER_NAME, required = false) changingAnswerForStep: String?,
        @RequestParam(STARTING_UPDATE_PARAMETER_NAME, required = false) startingUpdate: Boolean?,
    ): ModelAndView {
        if (!propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, principal.name)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Base user ${principal.name} is not the primary landlord of property ownership $propertyOwnershipId",
            )
        }

        if (startingUpdate == true) {
            journeyDataServiceFactory
                .create(PropertyDetailsUpdateJourneyFactory.getJourneyDataKey(propertyOwnershipId, stepName))
                .removeJourneyDataAndContextIdFromSession()
        }

        return propertyDetailsUpdateJourneyFactory
            .create(propertyOwnershipId, stepName, isChangingAnswer = changingAnswerForStep != null)
            .getModelAndViewForStep(changingAnswersForStep = changingAnswerForStep)
    }

    @PreAuthorize("hasRole('LANDLORD')")
    @PostMapping("$UPDATE_PROPERTY_DETAILS_ROUTE/{stepName}")
    fun postJourneyData(
        model: Model,
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
        @RequestParam(CHANGE_ANSWER_FOR_PARAMETER_NAME, required = false) changingAnswerForStep: String?,
    ): ModelAndView =
        if (propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, principal.name)) {
            propertyDetailsUpdateJourneyFactory
                .create(propertyOwnershipId, stepName, isChangingAnswer = changingAnswerForStep != null)
                .completeStep(formData, principal, changingAnswerForStep)
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
        val primaryLandlordDetailsUrl =
            LandlordDetailsController
                .getLandlordDetailsPath(propertyOwnership.primaryLandlord.id)
                .overrideBackLinkForUrl(backLinkStorageService.storeCurrentUrlReturningKey())

        val propertyDetails =
            PropertyDetailsViewModel(
                propertyOwnership = propertyOwnership,
                withChangeLinks = false,
                hideNullUprn = false,
                landlordDetailsUrl = primaryLandlordDetailsUrl,
            )

        val landlordViewModel =
            PropertyDetailsLandlordViewModelBuilder.fromEntity(
                propertyOwnership.primaryLandlord,
                primaryLandlordDetailsUrl,
            )

        model.addAttribute("propertyDetails", propertyDetails)
        model.addAttribute("lastModifiedDate", lastModifiedDate)
        model.addAttribute("lastModifiedBy", lastModifiedBy)
        model.addAttribute("landlordDetails", landlordViewModel)
        model.addAttribute("backUrl", LOCAL_AUTHORITY_DASHBOARD_URL)

        return "propertyDetailsView"
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
