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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController.Companion.PROPERTY_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.PageData
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.services.PropertyDeregistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@PrsdbController
@RequestMapping(PROPERTY_DEREGISTRATION_ROUTE)
class DeregisterPropertyController(
    private val propertyDeregistrationJourneyFactory: PropertyDeregistrationJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyDeregistrationService: PropertyDeregistrationService,
) {
    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
        principal: Principal,
    ): ModelAndView {
        throwExceptionIfCurrentUserIsUnauthorizedToDeregisterProperty(propertyOwnershipId, principal)

        return try {
            val journeyMap = propertyDeregistrationJourneyFactory.createJourneySteps(propertyOwnershipId)
            journeyMap[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            initializeAndRedirect(propertyOwnershipId, stepName)
        } catch (_: PropertyOwnershipMismatchException) {
            initializeAndRedirect(propertyOwnershipId, stepName)
        }
    }

    @PostMapping("/{stepName}")
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
        @RequestParam formData: PageData,
        principal: Principal,
    ): ModelAndView {
        throwExceptionIfCurrentUserIsUnauthorizedToDeregisterProperty(propertyOwnershipId, principal)

        return try {
            val journeyMap = propertyDeregistrationJourneyFactory.createJourneySteps(propertyOwnershipId)
            journeyMap[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            initializeAndRedirect(propertyOwnershipId, stepName)
        } catch (_: PropertyOwnershipMismatchException) {
            initializeAndRedirect(propertyOwnershipId, stepName)
        }
    }

    private fun initializeAndRedirect(
        propertyOwnershipId: Long,
        stepName: String,
    ): ModelAndView {
        val journeyId = propertyDeregistrationJourneyFactory.initializeJourneyState(propertyOwnershipId)
        val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
        return ModelAndView("redirect:$redirectUrl")
    }

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getConfirmation(
        model: Model,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
    ): String {
        checkPropertyHasBeenDeregisteredInThisSession(propertyOwnershipId)

        model.addAttribute("landlordDashboardUrl", LANDLORD_DASHBOARD_URL)

        return "deregisterPropertyConfirmation"
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

    private fun checkPropertyHasBeenDeregisteredInThisSession(propertyOwnershipId: Long) {
        if (propertyOwnershipId !in propertyDeregistrationService.getDeregisteredPropertyOwnershipIdsFromSession()) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "PropertyOwnershipId $propertyOwnershipId was not found in the list of deregistered propertyOwnershipIds in the session",
            )
        }

        if (propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId) != null) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Property ownership $propertyOwnershipId was found in the database",
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
            "${getPropertyDeregistrationBasePath(propertyOwnershipId)}/${AreYouSureStep.ROUTE_SEGMENT}"
    }
}
