package uk.gov.communities.prsdb.webapp.controllers

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
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController.Companion.PROPERTY_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordDashboardController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyDeregistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyService
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@Controller
@RequestMapping(PROPERTY_DEREGISTRATION_ROUTE)
class DeregisterPropertyController(
    private val propertyDeregistrationJourneyFactory: PropertyDeregistrationJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyService: PropertyService,
    private val propertyRegistrationService: PropertyRegistrationService,
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
        checkWasCurrentUserAuthorisedToDeregisterProperty(propertyOwnershipId)
        val propertyId = getDeregisteredPropertyEntityIdsFromSession().find { it.first == propertyOwnershipId }!!.second
        checkPropertyHasBeenDeregistered(propertyId, propertyOwnershipId)

        model.addAttribute("landlordDashboardUrl", LANDLORD_DASHBOARD_URL)

        return "deregisterPropertyConfirmation"
    }

    private fun getDeregisteredPropertyEntityIdsFromSession(): MutableList<Pair<Long, Long>> {
        val deregisteredEntityIds = propertyRegistrationService.getDeregisteredPropertyAndOwnershipIdsFromSession()
        if (deregisteredEntityIds.isEmpty()) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No deregistered Pair(propertyOwnershipId, propertyId) were found in the session",
            )
        }
        return deregisteredEntityIds
    }

    private fun checkWasCurrentUserAuthorisedToDeregisterProperty(propertyOwnershipId: Long) =
        checkPropertyOwnershipIdWasStoredInSessionOnDeregistration(propertyOwnershipId)

    private fun checkPropertyOwnershipIdWasStoredInSessionOnDeregistration(propertyOwnershipId: Long) {
        // A list of propertyOwnershipIds deregistered in this session is stored in the session.
        // This could only be done by a user who was authorised to deregister them.
        // If the propertyOwnershipId appears in the list, it is safe to show the confirmation page
        val deregisteredPropertyOwnershipIds = getDeregisteredPropertyEntityIdsFromSession().map { it.first }

        if (!deregisteredPropertyOwnershipIds.contains(propertyOwnershipId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "PropertyOwnershipId $propertyOwnershipId was not found in the list of deregistered propertyOwnershipIds in the session",
            )
        }
    }

    private fun checkPropertyHasBeenDeregistered(
        propertyId: Long,
        propertyOwnershipId: Long,
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
        const val PROPERTY_DEREGISTRATION_ROUTE = "/$DEREGISTER_PROPERTY_JOURNEY_URL/{propertyOwnershipId}"

        fun getPropertyDeregistrationPath(propertyOwnershipId: Long): String {
            val initialStepPathSegment = PropertyDeregistrationJourney.initialStepId.urlPathSegment

            return UriTemplate("$PROPERTY_DEREGISTRATION_ROUTE/$initialStepPathSegment")
                .expand(propertyOwnershipId)
                .toASCIIString()
        }
    }
}
