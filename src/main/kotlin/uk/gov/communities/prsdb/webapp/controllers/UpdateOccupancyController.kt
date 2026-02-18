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
import uk.gov.communities.prsdb.webapp.controllers.UpdateOccupancyController.Companion.UPDATE_ROUTE
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.UpdateOccupancyJourneyFactory
import java.security.Principal

@PrsdbController
@RequestMapping(UPDATE_ROUTE)
@PreAuthorize("hasRole('LANDLORD')")
class UpdateOccupancyController(
    private val journeyFactory: UpdateOccupancyJourneyFactory,
) {
    @GetMapping("{stepName}")
    fun getUpdateStep(
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
    ): ModelAndView =
        try {
            val journeyMap = journeyFactory.createJourneySteps(propertyOwnershipId)
            journeyMap[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = journeyFactory.initializeJourneyState(propertyOwnershipId, principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }

    @PostMapping("{stepName}")
    fun postUpdateStep(
        model: Model,
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
    ): ModelAndView =
        try {
            val journeyMap = journeyFactory.createJourneySteps(propertyOwnershipId)
            journeyMap[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = journeyFactory.initializeJourneyState(propertyOwnershipId, principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }

    companion object {
        const val UPDATE_ROUTE = "/$LANDLORD_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/{propertyOwnershipId}/update-occupancy"
        val UPDATE_OCCUPANCY_ROUTE = "$UPDATE_ROUTE/${RegisterPropertyStepId.Occupancy.urlPathSegment}"

        fun getUpdateOccupancyRoute(propertyOwnershipId: Long): String =
            UPDATE_OCCUPANCY_ROUTE.replace("{propertyOwnershipId}", propertyOwnershipId.toString())
    }
}
