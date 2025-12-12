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
            journeyStep.beforeIsStepReachable()
            if (journeyStep.isStepReachable) {
                journeyStep.afterIsStepReached()

                journeyStep.beforeGetPageVisitContent()
                val content = journeyStep.getPageVisitContent()
                journeyStep.afterGetPageVisitContent()

                journeyStep.beforeChooseTemplate()
                val destination = journeyStep.chooseTemplate().withModelContent(content)
                journeyStep.afterChooseTemplate()

                return destination.toModelAndView()
            }

            val unreachableStepDestination = journeyStep.getUnreachableStepDestination()
            return unreachableStepDestination.toModelAndView()
        }

        override fun postStepModelAndView(formData: PageData): ModelAndView {
            journeyStep.beforeIsStepReachable()
            if (journeyStep.isStepReachable) {
                journeyStep.afterIsStepReached()

                val newFormData = journeyStep.beforeValidateSubmittedData(formData)
                val bindingResult = journeyStep.validateSubmittedData(newFormData)
                journeyStep.afterValidateSubmittedData(bindingResult)

                if (bindingResult.hasErrors()) {
                    journeyStep.beforeGetPageVisitContent()
                    val content = journeyStep.getInvalidSubmissionContent(bindingResult)
                    journeyStep.afterGetPageVisitContent()

                    journeyStep.beforeChooseTemplate()
                    val destination = journeyStep.chooseTemplate().withModelContent(content)
                    journeyStep.afterChooseTemplate()

                    return destination.toModelAndView()
                }

                journeyStep.beforeSubmitFormData()
                journeyStep.submitFormData(bindingResult)
                journeyStep.afterSubmitFormData()

                journeyStep.beforeSaveState()
                journeyStep.saveState()
                journeyStep.afterSaveState()

                journeyStep.beforeDetermineNextDestination()
                val nextDestination = journeyStep.determineNextDestination()
                val finalDestination = journeyStep.afterDetermineNextDestination(nextDestination)

                return finalDestination.toModelAndView()
            }

            val unreachableStepDestination = journeyStep.getUnreachableStepDestination()
            return unreachableStepDestination.toModelAndView()
        }
    }

    class RedirectingStepLifecycleOrchestrator(
        journeyStep: JourneyStep<*, *, *>,
    ) : StepLifecycleOrchestrator(journeyStep) {
        override fun getStepModelAndView(): ModelAndView {
            journeyStep.beforeIsStepReachable()
            if (journeyStep.isStepReachable) {
                journeyStep.afterIsStepReached()

                journeyStep.beforeSaveState()
                journeyStep.saveState()
                journeyStep.afterSaveState()

                journeyStep.beforeDetermineNextDestination()
                val nextDestination = journeyStep.determineNextDestination()
                val finalDestination = journeyStep.afterDetermineNextDestination(nextDestination)

                return finalDestination.toModelAndView()
            }

            val unreachableStepDestination = journeyStep.getUnreachableStepDestination()
            return unreachableStepDestination.toModelAndView()
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
