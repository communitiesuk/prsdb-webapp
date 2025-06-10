package uk.gov.communities.prsdb.webapp.forms.tasks

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper

class JourneyTask<T : StepId>(
    val startingStepId: T,
    val steps: Set<Step<T>>,
    val nameKey: String? = null,
    val hintKey: String? = null,
) {
    fun getTaskStatus(
        filteredJourneyData: JourneyData,
        validator: Validator,
        isTaskReachable: Boolean = true,
    ) = if (!isTaskReachable) {
        TaskStatus.CANNOT_START
    } else if (!isInitialStepComplete(filteredJourneyData, validator)) {
        TaskStatus.NOT_STARTED
    } else if (!areAllStepsWithinTaskComplete(filteredJourneyData, validator)) {
        TaskStatus.IN_PROGRESS
    } else {
        TaskStatus.COMPLETED
    }

    private fun isInitialStepComplete(
        filteredJourneyData: JourneyData,
        validator: Validator,
    ): Boolean =
        isStepComplete(
            filteredJourneyData,
            steps.single { it.id == startingStepId },
            validator,
        )

    private fun areAllStepsWithinTaskComplete(
        filteredJourneyData: JourneyData,
        validator: Validator,
    ): Boolean {
        var currentStep: Step<T>? = steps.single { it.id == startingStepId }
        while (currentStep != null && currentStep in steps) {
            if (isStepComplete(filteredJourneyData, currentStep, validator)) {
                val nextStepId = currentStep.nextAction(filteredJourneyData, null).first
                currentStep = steps.singleOrNull { it.id == nextStepId }
            } else {
                return false
            }
        }
        return true
    }

    private fun isStepComplete(
        filteredJourneyData: JourneyData,
        step: Step<T>,
        validator: Validator,
    ): Boolean {
        val pageData = JourneyDataHelper.getPageData(filteredJourneyData, step.name)
        val bindingResult = step.page.bindDataToFormModel(validator, pageData)
        return pageData != null && step.isSatisfied(bindingResult)
    }

    companion object {
        fun <T : StepId> withOneStep(
            onlyStep: Step<T>,
            nameKey: String? = null,
            hintKey: String? = null,
        ) = JourneyTask(onlyStep.id, setOf(onlyStep), nameKey, hintKey)
    }
}
