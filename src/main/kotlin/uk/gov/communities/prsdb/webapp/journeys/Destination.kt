package uk.gov.communities.prsdb.webapp.journeys

import org.springframework.web.servlet.ModelAndView

sealed class Destination {
    fun toModelAndView(): ModelAndView =
        when (this) {
            is VisitableStep -> ModelAndView("redirect:${step.routeSegment}", mapOf("journeyId" to journeyId))
            is ExternalUrl -> ModelAndView("redirect:$externalUrl", params)
            is Template -> ModelAndView(templateName, content)
            is NavigationalStep -> StepLifecycleOrchestrator(step).getStepModelAndView()
        }

    fun withModelContent(content: Map<String, Any?>): Destination =
        when (this) {
            is Template -> Template(templateName, this.content + content)
            else -> this
        }

    class VisitableStep(
        val step: JourneyStep.RoutedStep<*, *, *>,
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

    class NavigationalStep(
        val step: JourneyStep.UnroutedStep<*, *, *>,
    ) : Destination()

    companion object {
        operator fun invoke(step: JourneyStep<*, *, *>): Destination =
            when (step) {
                is JourneyStep.RoutedStep -> VisitableStep(step, step.currentJourneyId)
                is JourneyStep.UnroutedStep -> NavigationalStep(step)
            }
    }
}
