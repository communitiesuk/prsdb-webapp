package uk.gov.communities.prsdb.webapp.journeys.example

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep

sealed class Destination {
    fun toModelAndView(): ModelAndView =
        when (this) {
            is Step -> ModelAndView("redirect:${step.routeSegment}", params)
            is ExternalUrl -> ModelAndView("redirect:$externalUrl", params)
            is Template -> ModelAndView(templateName, content)
        }

    fun withContent(content: Map<String, Any?>): Destination =
        when (this) {
            is Template -> Template(templateName, this.content + content)
            else -> this
        }

    fun withParam(
        key: String,
        value: String,
    ): Destination =
        when (this) {
            is Step -> Step(step, params + (key to value))
            is ExternalUrl -> ExternalUrl(externalUrl, params + (key to value))
            is Template -> this
        }

    class Step(
        val step: JourneyStep<*, *, *>,
        val params: Map<String, String> = mapOf(),
    ) : Destination()

    class ExternalUrl(
        val externalUrl: String,
        val params: Map<String, String> = mapOf(),
    ) : Destination()

    class Template(
        val templateName: String,
        val content: Map<String, Any?> = mapOf(),
    ) : Destination()
}
