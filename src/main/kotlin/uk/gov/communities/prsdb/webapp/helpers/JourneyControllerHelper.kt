package uk.gov.communities.prsdb.webapp.helpers

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator

object JourneyControllerHelper {
    fun handleGetRequest(
        buildJourneyRoutingMap: () -> Map<String, StepLifecycleOrchestrator>,
        stepRouteSegment: String,
        initializeJourney: () -> String,
    ) = handleRequest(
        buildJourneyRoutingMap,
        stepRouteSegment,
        { this.getStepModelAndView() },
        initializeJourney,
    )

    fun handlePostRequest(
        buildJourneyRoutingMap: () -> Map<String, StepLifecycleOrchestrator>,
        stepRouteSegment: String,
        initializeJourney: () -> String,
        formData: PageData,
    ) = handleRequest(
        buildJourneyRoutingMap,
        stepRouteSegment,
        { this.postStepModelAndView(formData) },
        initializeJourney,
    )

    private fun handleRequest(
        buildJourneyRoutingMap: () -> Map<String, StepLifecycleOrchestrator>,
        stepRouteSegment: String,
        callStepLifecycleMethod: StepLifecycleOrchestrator.() -> ModelAndView,
        initializeJourney: () -> String,
    ) = try {
        val journeyMap = buildJourneyRoutingMap()
        val journeyStep = journeyMap[stepRouteSegment] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        journeyStep.callStepLifecycleMethod()
    } catch (_: NoSuchJourneyException) {
        val journeyId = initializeJourney()
        val redirectUrl = JourneyStateService.urlWithJourneyState(stepRouteSegment, journeyId)
        ModelAndView("redirect:$redirectUrl")
    }
}
