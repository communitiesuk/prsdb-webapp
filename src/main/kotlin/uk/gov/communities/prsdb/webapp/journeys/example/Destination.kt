package uk.gov.communities.prsdb.webapp.journeys.example

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NavigationalStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.example.steps.NavigationalStep

sealed class Destination {
    fun toModelAndView(): ModelAndView =
        when (this) {
            is Step -> ModelAndView("redirect:${step.routeSegment}", mapOf("journeyId" to journeyId))
            is ExternalUrl -> ModelAndView("redirect:$externalUrl", params)
            is Template -> ModelAndView(templateName, content)
            is Navigation -> NavigationalStepLifecycleOrchestrator(step).getStepModelAndView()
        }

    fun withModelContent(content: Map<String, Any?>): Destination =
        when (this) {
            is Template -> Template(templateName, this.content + content)
            else -> this
        }

    class Step(
        val step: JourneyStep<*, *, *>,
        val journeyId: String,
    ) : Destination()

    class ExternalUrl(
        val externalUrl: String,
        val params: Map<String, String> = mapOf(),
    ) : Destination()

    class Template(
        val templateName: String,
        val content: Map<String, Any?> = mapOf(),
    ) : Destination()

    class Navigation(
        val step: NavigationalStep<*>,
    ) : Destination()

    companion object {
        operator fun invoke(step: JourneyStep<*, *, *>): Destination =
            if (step is NavigationalStep<*>) {
                Navigation(step)
            } else {
                Step(step, step.currentJourneyId)
            }
    }
}
