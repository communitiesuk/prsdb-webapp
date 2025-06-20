package uk.gov.communities.prsdb.webapp.controllers

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
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController.Companion.PROPERTY_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyDeregistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyDeregistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyService
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@PrsdbController
@RequestMapping(PROPERTY_DEREGISTRATION_ROUTE)
class DeregisterPropertyController(
    private val propertyDeregistrationJourneyFactory: PropertyDeregistrationJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyService: PropertyService,
    private val propertyDeregistrationService: PropertyDeregistrationService,
) {
    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        model: Model,
        principal: Principal,
    ): ModelAndView {
        throwExceptionIfCurrentUserIsUnauthorizedToDeregisterProperty(propertyOwnershipId, principal)

        return propertyDeregistrationJourneyFactory
            .create(propertyOwnershipId)
            .getModelAndViewForStep(
                stepName,
                subpage,
            )
    }

    @PostMapping("/{stepName}")
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam formData: PageData,
        model: Model,
        principal: Principal,
    ): ModelAndView {
        throwExceptionIfCurrentUserIsUnauthorizedToDeregisterProperty(propertyOwnershipId, principal)

        return propertyDeregistrationJourneyFactory
            .create(propertyOwnershipId)
            .completeStep(
                stepName,
                formData,
                subpage,
                principal,
            )
    }

    private fun throwExceptionIfCurrentUserIsUnauthorizedToDeregisterProperty(
        propertyOwnershipId: Long,
        principal: Principal,
    ) {
        if (!isCurrentUserAuthorizedToDeregisterProperty(propertyOwnershipId, principal)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "The current user is not authorised to delete property ownership $propertyOwnershipId",
            )
        }
    }

    private fun isCurrentUserAuthorizedToDeregisterProperty(
        propertyOwnershipId: Long,
        principal: Principal,
    ): Boolean =
        propertyOwnershipService
            .getIsPrimaryLandlord(propertyOwnershipId, principal.name)

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getConfirmation(
        model: Model,
        principal: Principal,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
    ): String {
        val propertyId = getPropertyIdIfPropertyWasDeregisteredThisSession(propertyOwnershipId)
        checkPropertyHasBeenDeregistered(propertyOwnershipId, propertyId)

        model.addAttribute("landlordDashboardUrl", LANDLORD_DASHBOARD_URL)

        return "deregisterPropertyConfirmation"
    }

    private fun getPropertyIdIfPropertyWasDeregisteredThisSession(propertyOwnershipId: Long): Long {
        val entityIdsDeregisteredThisSession = propertyDeregistrationService.getDeregisteredPropertyAndOwnershipIdsFromSession()
        if (entityIdsDeregisteredThisSession.isEmpty()) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No deregistered Pair(propertyOwnershipId, propertyId) were found in the session",
            )
        }

        val deregisteredPropertyIdPair = entityIdsDeregisteredThisSession.find { it.first == propertyOwnershipId }
        if (deregisteredPropertyIdPair == null) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "PropertyOwnershipId $propertyOwnershipId was not found in the list of deregistered propertyOwnershipIds in the session",
            )
        }
        return deregisteredPropertyIdPair.second
    }

    private fun checkPropertyHasBeenDeregistered(
        propertyOwnershipId: Long,
        propertyId: Long,
    ) {
        if (propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId) != null) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Property ownership $propertyOwnershipId was found in the database",
            )
        }

        if (propertyService.retrievePropertyById(propertyId) != null) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Property $propertyId was found in the database",
            )
        }
    }

    companion object {
        const val PROPERTY_DEREGISTRATION_ROUTE = "/$LANDLORD_PATH_SEGMENT/$DEREGISTER_PROPERTY_JOURNEY_URL/{propertyOwnershipId}"

        fun getPropertyDeregistrationBasePath(propertyOwnershipId: Long): String =
            UriTemplate(PROPERTY_DEREGISTRATION_ROUTE)
                .expand(propertyOwnershipId)
                .toASCIIString()

        fun getPropertyDeregistrationPath(propertyOwnershipId: Long): String =
            "${getPropertyDeregistrationBasePath(propertyOwnershipId)}/${PropertyDeregistrationJourney.initialStepId.urlPathSegment}"
    }
}
