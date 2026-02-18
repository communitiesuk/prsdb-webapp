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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipTypeController.Companion.UPDATE_ROUTE
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownershipType.UpdateOwnershipTypeJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbController
@RequestMapping(UPDATE_ROUTE)
@PreAuthorize("hasRole('LANDLORD')")
class UpdateOwnershipTypeController(
    private val journeyFactory: UpdateOwnershipTypeJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    @GetMapping("{stepName}")
    fun getUpdateStep(
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
    ): ModelAndView =
        if (propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, principal.name)) {
            try {
                val journeyMap = journeyFactory.createJourneySteps(propertyOwnershipId)
                journeyMap[stepName]?.getStepModelAndView()
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
            } catch (_: NoSuchJourneyException) {
                val journeyId = journeyFactory.initializeJourneyState(propertyOwnershipId, principal)
                val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
                ModelAndView("redirect:$redirectUrl")
            }
        } else {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Base user ${principal.name} is not the primary landlord of property ownership $propertyOwnershipId",
            )
        }

    @PostMapping("{stepName}")
    fun postUpdateStep(
        model: Model,
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
    ): ModelAndView =
        if (propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, principal.name)) {
            try {
                val journeyMap = journeyFactory.createJourneySteps(propertyOwnershipId)
                journeyMap[stepName]?.postStepModelAndView(formData)
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
            } catch (_: NoSuchJourneyException) {
                val journeyId = journeyFactory.initializeJourneyState(propertyOwnershipId, principal)
                val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
                ModelAndView("redirect:$redirectUrl")
            }
        } else {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Base user ${principal.name} is not the primary landlord of property ownership $propertyOwnershipId",
            )
        }

    companion object {
        const val UPDATE_ROUTE = "/$LANDLORD_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/{propertyOwnershipId}/update-ownership-type"
        val UPDATE_OWNERSHIP_TYPE_ROUTE = "$UPDATE_ROUTE/${RegisterPropertyStepId.OwnershipType.urlPathSegment}"

        fun getUpdateOwnershipTypeRoute(propertyOwnershipId: Long): String =
            UPDATE_OWNERSHIP_TYPE_ROUTE.replace("{propertyOwnershipId}", propertyOwnershipId.toString())
    }
}
