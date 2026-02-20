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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MIGRATE_PROPERTY_DEREGISTRATION
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController.Companion.PROPERTY_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.NewPropertyDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyDeregistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@PrsdbController
@RequestMapping(PROPERTY_DEREGISTRATION_ROUTE)
class NewDeregisterPropertyController(
    private val propertyDeregistrationJourneyFactory: NewPropertyDeregistrationJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyDeregistrationService: PropertyDeregistrationService,
) {
    @GetMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_PROPERTY_DEREGISTRATION)
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
    @AvailableWhenFeatureEnabled(MIGRATE_PROPERTY_DEREGISTRATION)
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
    @AvailableWhenFeatureEnabled(MIGRATE_PROPERTY_DEREGISTRATION)
    fun getConfirmation(
        model: Model,
        principal: Principal,
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
}
