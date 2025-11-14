package uk.gov.communities.prsdb.webapp.journeys

import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriComponentsBuilder
import kotlin.collections.component1
import kotlin.collections.component2

sealed class Destination {
    abstract fun toModelAndView(): ModelAndView

    abstract fun toUrlStringOrNull(): String?

    fun withModelContent(content: Map<String, Any?>): Destination =
        when (this) {
            is Template -> Template(templateName, this.content + content)
            else -> this
        }

    class VisitableStep(
        val step: JourneyStep.RequestableStep<*, *, *>,
        val journeyId: String,
    ) : Destination() {
        override fun toModelAndView() = ModelAndView("redirect:${step.routeSegment}", mapOf("journeyId" to journeyId))

        override fun toUrlStringOrNull() =
            if (step.isStepReachable) JourneyStateService.urlWithJourneyState(step.routeSegment, journeyId) else null
    }

    class ExternalUrl(
        val externalUrl: String,
        val params: Map<String, String> = mapOf(),
    ) : Destination() {
        override fun toModelAndView() = ModelAndView("redirect:$externalUrl", params)

        override fun toUrlStringOrNull() =
            UriComponentsBuilder
                .fromUriString(externalUrl)
                .apply {
                    params.forEach { (key, value) -> queryParam(key, value) }
                }.toUriString()
    }

    class Template(
        val templateName: String,
        val content: Map<String, Any?> = mapOf(),
    ) : Destination() {
        override fun toModelAndView() = ModelAndView(templateName, content)

        override fun toUrlStringOrNull() = null
    }

    class NavigationalStep(
        val step: JourneyStep.InternalStep<*, *, *>,
    ) : Destination() {
        override fun toModelAndView() = StepLifecycleOrchestrator(step).getStepModelAndView()

        override fun toUrlStringOrNull() = if (step.isStepReachable) step.determineNextDestination().toUrlStringOrNull() else null
    }

    companion object {
        operator fun invoke(step: JourneyStep<*, *, *>): Destination =
            when (step) {
                is JourneyStep.RequestableStep -> VisitableStep(step, step.currentJourneyId)
                is JourneyStep.InternalStep -> NavigationalStep(step)
            }
    }
}
