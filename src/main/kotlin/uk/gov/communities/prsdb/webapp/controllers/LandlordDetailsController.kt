package uk.gov.communities.prsdb.webapp.controllers

import kotlinx.datetime.toKotlinInstant
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.UPDATE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlord
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.LandlordViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.OrgLandlordViewModel
import uk.gov.communities.prsdb.webapp.services.BackUrlStorageService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.OrganisationGoverningBodyMemberService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@PrsdbController
@RequestMapping
class LandlordDetailsController(
    private val landlordService: LandlordService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val backUrlStorageService: BackUrlStorageService,
    private val userToLandlordService: UserToLandlordService,
    private val organisationGoverningBodyMemberService: OrganisationGoverningBodyMemberService,
    private val featureFlagManager: FeatureFlagManager,
) {
    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping(LANDLORD_DETAILS_FOR_LANDLORD_ROUTE)
    fun getUserLandlordDetails(model: Model): String {
        val landlord = userToLandlordService.getCurrentLandlordForUser()

        if (landlord is OrganisationLandlord) {
            if (!featureFlagManager.checkFeature(ORGANISATION_LANDLORD_REGISTRATION)) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Organisation landlords are not currently available")
            }
            return getOrgLandlordDetails(landlord, model)
        }

        val landlordViewModel = LandlordViewModel(landlord as IndividualLandlord, withChangeLinks = true)

        model.addAttribute("landlord", landlordViewModel)

        addUserLandlordDetailsSharedAttributes(landlord, model)

        return "landlordDetailsView"
    }

    // TODO: PDJB-1474 (details tab) & PDJB-1475 (contacts tab): Replace this skeleton page with proper summary list content
    private fun getOrgLandlordDetails(
        orgLandlord: OrganisationLandlord,
        model: Model,
    ): String {
        val governingBodyMembers =
            organisationGoverningBodyMemberService.getGoverningBodyMembers(orgLandlord)

        model.addAttribute("orgLandlord", OrgLandlordViewModel(orgLandlord))
        model.addAttribute("governingBodyMembers", governingBodyMembers)

        addUserLandlordDetailsSharedAttributes(orgLandlord, model)

        return "orgLandlordDetailsView"
    }

    private fun addUserLandlordDetailsSharedAttributes(
        landlord: Landlord,
        model: Model,
    ) {
        val registeredPropertiesList =
            propertyOwnershipService.getRegisteredPropertiesForLandlordUser(
                landlord,
                currentUrlFragment = REGISTERED_PROPERTIES_FRAGMENT,
            )
        model.addAttribute("registeredPropertiesList", registeredPropertiesList)
        model.addAttribute("registeredPropertiesTabId", REGISTERED_PROPERTIES_FRAGMENT)

        val backUrlKey = backUrlStorageService.storeCurrentUrlReturningKey(REGISTERED_PROPERTIES_FRAGMENT)
        model.addAttribute(
            "registerPropertyUrl",
            RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE.overrideBackLinkForUrl(backUrlKey),
        )
        model.addAttribute("deleteLandlordRecordUrl", DeregisterLandlordController.LANDLORD_DEREGISTRATION_PATH)
        model.addAttribute("backUrl", LANDLORD_DASHBOARD_URL)
    }

    @PreAuthorize("hasAnyRole('LOCAL_COUNCIL_USER', 'LOCAL_COUNCIL_ADMIN')")
    @GetMapping(LANDLORD_DETAILS_FOR_LOCAL_COUNCIL_USER_ROUTE)
    fun getLandlordDetails(
        @PathVariable id: Long,
        model: Model,
    ): String {
        val landlord =
            landlordService.retrieveLandlordById(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Landlord $id not found")

        val lastModifiedDate = DateTimeHelper.getDateInUK(landlord.getMostRecentlyUpdated().toKotlinInstant())

        val landlordViewModel = LandlordViewModel(landlord as IndividualLandlord, withChangeLinks = false)

        model.addAttribute("lastModifiedDate", lastModifiedDate)
        model.addAttribute("landlord", landlordViewModel)
        model.addAttribute("registeredPropertiesTabId", REGISTERED_PROPERTIES_FRAGMENT)

        val registeredPropertiesList =
            propertyOwnershipService.getRegisteredPropertiesForLandlord(
                id,
                currentUrlFragment = REGISTERED_PROPERTIES_FRAGMENT,
            )

        model.addAttribute("registeredPropertiesList", registeredPropertiesList)

        model.addAttribute("backUrl", "/")

        return "localCouncilLandlordDetailsView"
    }

    companion object {
        const val LANDLORD_DETAILS_FOR_LANDLORD_ROUTE = "/$LANDLORD_PATH_SEGMENT/$LANDLORD_DETAILS_PATH_SEGMENT"
        const val LANDLORD_DETAILS_FOR_LOCAL_COUNCIL_USER_ROUTE = "/$LOCAL_COUNCIL_PATH_SEGMENT/$LANDLORD_DETAILS_PATH_SEGMENT/{id}"
        const val UPDATE_ROUTE = "$LANDLORD_DETAILS_FOR_LANDLORD_ROUTE/$UPDATE_PATH_SEGMENT"

        fun getLandlordDetailsForLocalCouncilUserPath(landlordId: Long? = null): String =
            UriTemplate(LANDLORD_DETAILS_FOR_LOCAL_COUNCIL_USER_ROUTE)
                .expand(landlordId)
                .toASCIIString()
    }
}
