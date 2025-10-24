package uk.gov.communities.prsdb.webapp.journeys

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.PageData

class StepLifecycleOrchestrator(
    val journeyStep: JourneyStep<*, *, *>,
) {
    fun getStepModelAndView(): ModelAndView {
        journeyStep.beforeIsStepReachable()
        if (journeyStep.isStepReachable) {
            journeyStep.afterIsStepReached()

            journeyStep.beforeGetStepContent()
            val content = journeyStep.getPageVisitContent()
            journeyStep.afterGetStepContent()

            journeyStep.beforeGetTemplate()
            val destination = journeyStep.chooseVisitDestination().withContent(content)
            journeyStep.afterGetTemplate()

            return destination.toModelAndView()
        }

        val unreachableStepDestination = journeyStep.getUnreachableStepDestination()
        return unreachableStepDestination.toModelAndView()
    }

    fun postStepModelAndView(formData: PageData): ModelAndView {
        journeyStep.beforeIsStepReachable()
        if (journeyStep.isStepReachable) {
            journeyStep.afterIsStepReached()

            val newFormData = journeyStep.beforeValidateSubmittedData(formData)
            val bindingResult = journeyStep.validateSubmittedData(newFormData)
            journeyStep.afterValidateSubmittedData(bindingResult)

            if (bindingResult.hasErrors()) {
                journeyStep.beforeGetStepContent()
                val content = journeyStep.getInvalidSubmissionContent(bindingResult)
                journeyStep.afterGetStepContent()

                journeyStep.beforeGetTemplate()
                val destination = journeyStep.chooseVisitDestination().withContent(content)
                journeyStep.afterGetTemplate()

                return destination.toModelAndView()
            }

            journeyStep.beforeSubmitFormData()
            journeyStep.submitFormData(bindingResult)
            journeyStep.afterSubmitFormData()

            journeyStep.beforeDetermineRedirect()
            val nextDestination = journeyStep.determineNextDestination()
            journeyStep.afterDetermineRedirect()

            return nextDestination.toModelAndView()
        }

        val unreachableStepDestination = journeyStep.getUnreachableStepDestination()
        return unreachableStepDestination.toModelAndView()
    }
}
