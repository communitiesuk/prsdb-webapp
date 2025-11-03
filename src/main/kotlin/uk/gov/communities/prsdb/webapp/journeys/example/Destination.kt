package uk.gov.communities.prsdb.webapp.journeys.example

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep

sealed class Destination {
    fun toModelAndView(): ModelAndView =
        when (this) {
            is Step -> ModelAndView("redirect:${step.routeSegment}", mapOf("journeyId" to journeyId))
            is ExternalUrl -> ModelAndView("redirect:$externalUrl", params)
            is Template -> ModelAndView(templateName, content)
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

    companion object {
        operator fun invoke(step: JourneyStep<*, *, *>): Destination = Step(step, step.currentJourneyId)
    }
}
