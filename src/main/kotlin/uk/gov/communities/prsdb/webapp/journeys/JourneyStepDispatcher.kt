package uk.gov.communities.prsdb.webapp.journeys

import org.springframework.http.HttpStatus
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView

object JourneyStepDispatcher {
    fun handleInitialisableRequest(
        rawStepPath: String,
        createRoutingMap: () -> Map<String, StepLifecycleOrchestrator>,
        initialiseJourney: () -> String,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
        startNewJourneyOn: (Throwable) -> Boolean = { false },
    ): ModelAndView {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        request.setAttribute(JourneyStateService.JOURNEY_BASE_PATH_ATTRIBUTE, deriveJourneyBasePath(request.requestURI, rawStepPath))
        val stepPath = rawStepPath.trimStart('/')
        if (stepPath.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        }
        return try {
            val routingMap = createRoutingMap()
            routingMap[stepPath]?.dispatch()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (exception: Exception) {
            if (exception is NoSuchJourneyException || startNewJourneyOn(exception)) {
                Destination.StepRoute(stepPath, initialiseJourney()).toModelAndView()
            } else {
                throw exception
            }
        }
    }

    fun handleUninitialisableRequest(
        rawStepPath: String,
        createRoutingMap: () -> Map<String, StepLifecycleOrchestrator>,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
        redirectOn: (Throwable) -> Boolean = { false },
        getRedirect: () -> ModelAndView,
    ): ModelAndView {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        request.setAttribute(JourneyStateService.JOURNEY_BASE_PATH_ATTRIBUTE, deriveJourneyBasePath(request.requestURI, rawStepPath))
        val stepPath = rawStepPath.trimStart('/')
        if (stepPath.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        }
        return try {
            val routingMap = createRoutingMap()
            routingMap[stepPath]?.dispatch()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (exception: Exception) {
            if (exception is NoSuchJourneyException || redirectOn(exception)) {
                getRedirect()
            } else {
                throw exception
            }
        }
    }

    private fun deriveJourneyBasePath(
        requestUri: String,
        rawStepPath: String,
    ): String {
        val stepSuffix = if (rawStepPath.startsWith("/")) rawStepPath else "/$rawStepPath"
        return requestUri.removeSuffix(stepSuffix)
    }
}
