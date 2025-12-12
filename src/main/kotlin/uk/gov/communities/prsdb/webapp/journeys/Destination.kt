package uk.gov.communities.prsdb.webapp.journeys

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriComponentsBuilder
import kotlin.collections.component1
import kotlin.collections.component2

sealed class Destination {
    abstract fun toModelAndView(): ModelAndView

    abstract fun toUrlStringOrNull(): String?

    open fun withModelContent(content: Map<String, Any?>): Destination = this

    class VisitableStep(
        val step: JourneyStep.RequestableStep<*, *, *>,
        val journeyId: String,
    ) : Destination() {
        override fun toModelAndView() =
            ModelAndView("redirect:${JourneyStateService.urlWithJourneyState(step.routeSegment, journeyId)}", mapOf<String, String>())

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

        override fun withModelContent(content: Map<String, Any?>) = Template(templateName, this.content + content)

        override fun toUrlStringOrNull() = null
    }

    class NavigationalStep(
        val step: JourneyStep.InternalStep<*, *, *>,
    ) : Destination() {
        override fun toModelAndView() = StepLifecycleOrchestrator(step).getStepModelAndView()

        override fun toUrlStringOrNull() = if (step.isStepReachable) step.getNextDestination().toUrlStringOrNull() else null
    }

    class Nowhere : Destination() {
        override fun toModelAndView(): ModelAndView = throw ResponseStatusException(HttpStatus.NOT_FOUND, "Navigated to Nowhere")

        override fun toUrlStringOrNull(): String? = null
    }

    companion object {
        operator fun invoke(step: JourneyStep<*, *, *>?): Destination =
            when (step) {
                is JourneyStep.RequestableStep -> VisitableStep(step, step.currentJourneyId)
                is JourneyStep.InternalStep -> NavigationalStep(step)
                null -> Nowhere()
            }
    }
}
