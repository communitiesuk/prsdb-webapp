package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.JOIN_PROPERTY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.START_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.JoinPropertyJourneyFactory
import java.security.Principal

@PreAuthorize("hasAnyRole('LANDLORD')")
@PrsdbController
@RequestMapping(JOIN_PROPERTY_ROUTE)
class JoinPropertyController(
    private val joinPropertyJourneyFactory: JoinPropertyJourneyFactory,
) {
    @GetMapping("/{stepRouteSegment}")
    fun getJourneyStep(
        @PathVariable stepRouteSegment: String,
        principal: Principal,
    ): ModelAndView =
        try {
            val journeyMap = joinPropertyJourneyFactory.createJourneySteps()
            journeyMap[stepRouteSegment]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = joinPropertyJourneyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepRouteSegment, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }

    @PostMapping("/{stepRouteSegment}")
    fun postJourneyData(
        @PathVariable stepRouteSegment: String,
        @RequestParam formData: PageData,
        principal: Principal,
    ): ModelAndView =
        try {
            val journeyMap = joinPropertyJourneyFactory.createJourneySteps()
            journeyMap[stepRouteSegment]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = joinPropertyJourneyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepRouteSegment, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }

    companion object {
        const val JOIN_PROPERTY_ROUTE = "/$LANDLORD_PATH_SEGMENT/$JOIN_PROPERTY_PATH_SEGMENT"
        const val JOIN_PROPERTY_START_PAGE_ROUTE = "$JOIN_PROPERTY_ROUTE/$START_PAGE_PATH_SEGMENT"
    }
}
