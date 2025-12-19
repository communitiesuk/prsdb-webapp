package uk.gov.communities.prsdb.webapp.journeys

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.PageData

sealed class StepLifecycleOrchestrator(
    val journeyStep: JourneyStep<*, *, *>,
) {
    abstract fun getStepModelAndView(): ModelAndView

    abstract fun postStepModelAndView(formData: PageData): ModelAndView

    class VisitableStepLifecycleOrchestrator(
        journeyStep: JourneyStep<*, *, *>,
    ) : StepLifecycleOrchestrator(journeyStep) {
        override fun getStepModelAndView(): ModelAndView {
            if (journeyStep.attemptToReachStep()) {
                val content = journeyStep.getPageVisitContent()

                return journeyStep.chooseTemplate().withModelContent(content).toModelAndView()
            }

            return journeyStep.getUnreachableStepDestination().toModelAndView()
        }

        override fun postStepModelAndView(formData: PageData): ModelAndView {
            if (journeyStep.attemptToReachStep()) {
                val bindingResult = journeyStep.validateSubmittedData(formData)

                if (!bindingResult.hasErrors()) {
                    journeyStep.submitFormData(bindingResult)

                    journeyStep.saveStateIfAllowed()
                    return journeyStep.getNextDestination().toModelAndView()
                }

                val content = journeyStep.getInvalidSubmissionContent(bindingResult)

                return journeyStep.chooseTemplate().withModelContent(content).toModelAndView()
            }

            return journeyStep.getUnreachableStepDestination().toModelAndView()
        }
    }

    class RedirectingStepLifecycleOrchestrator(
        journeyStep: JourneyStep<*, *, *>,
    ) : StepLifecycleOrchestrator(journeyStep) {
        override fun getStepModelAndView(): ModelAndView {
            if (journeyStep.attemptToReachStep()) {
                journeyStep.saveStateIfAllowed()
                return journeyStep.getNextDestination().toModelAndView()
            }

            return journeyStep.getUnreachableStepDestination().toModelAndView()
        }

        override fun postStepModelAndView(formData: PageData): ModelAndView = journeyStep.getUnreachableStepDestination().toModelAndView()
    }

    companion object {
        operator fun invoke(journeyStep: JourneyStep<*, *, *>) =
            when (journeyStep) {
                is JourneyStep.RequestableStep -> VisitableStepLifecycleOrchestrator(journeyStep)
                is JourneyStep.InternalStep -> RedirectingStepLifecycleOrchestrator(journeyStep)
            }
    }
}
