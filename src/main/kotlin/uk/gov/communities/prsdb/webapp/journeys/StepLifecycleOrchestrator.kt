package uk.gov.communities.prsdb.webapp.journeys

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.PageData

class StepLifecycleOrchestrator(
    val innerStep: AbstractStep<*, *, *>,
) {
    fun getStepModelAndView(): ModelAndView {
        innerStep.beforeIsStepReachable()
        if (innerStep.isStepReachable) {
            innerStep.afterIsStepReached()

            innerStep.beforeGetStepContent()
            val content = innerStep.getPageVisitContent()
            innerStep.afterGetStepContent()

            innerStep.beforeGetTemplate()
            val template = innerStep.chooseTemplate()
            innerStep.afterGetTemplate()

            return ModelAndView(template, content)
        }

        val unreachableStepRedirect = innerStep.getUnreachableStepRedirect
        return ModelAndView("redirect:$unreachableStepRedirect")
    }

    fun postStepModelAndView(formData: PageData): ModelAndView {
        innerStep.beforeIsStepReachable()
        if (innerStep.isStepReachable) {
            innerStep.afterIsStepReached()

            val newFormData = innerStep.beforeValidateSubmittedData(formData)
            val bindingResult = innerStep.validateSubmittedData(newFormData)
            innerStep.afterValidateSubmittedData(bindingResult)

            if (bindingResult.hasErrors()) {
                innerStep.beforeGetStepContent()
                val content = innerStep.getInvalidSubmissionContent(bindingResult)
                innerStep.afterGetStepContent()

                innerStep.beforeGetTemplate()
                val template = innerStep.chooseTemplate()
                innerStep.afterGetTemplate()

                return ModelAndView(template, content)
            }

            innerStep.beforeSubmitFormData()
            innerStep.submitFormData(formData)
            innerStep.afterSubmitFormData()

            innerStep.beforeDetermineRedirect()
            val redirect = innerStep.determineRedirect()
            innerStep.afterDetermineRedirect()

            return ModelAndView("redirect:$redirect")
        }

        val unreachableStepRedirect = innerStep.getUnreachableStepRedirect
        return ModelAndView("redirect:$unreachableStepRedirect")
    }
}
