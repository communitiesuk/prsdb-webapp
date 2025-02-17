package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper

class JourneyTask<T : StepId>(
    val startingStepId: T,
    val steps: Set<Step<T>>,
    val nameKey: String? = null,
) {
    fun getTaskStatus(
        journeyData: JourneyData,
        validator: Validator,
        isTaskReachable: Boolean = true,
    ) = if (!isTaskReachable) {
        TaskStatus.CANNOT_START_YET
    } else if (!isInitialStepComplete(journeyData, validator)) {
        TaskStatus.NOT_YET_STARTED
    } else if (!areAllStepsWithinTaskComplete(journeyData, validator)) {
        TaskStatus.IN_PROGRESS
    } else {
        TaskStatus.COMPLETED
    }

    private fun isInitialStepComplete(
        journeyData: JourneyData,
        validator: Validator,
    ): Boolean =
        isStepComplete(
            journeyData,
            steps.single { it.id == startingStepId },
            validator,
        )

    private fun areAllStepsWithinTaskComplete(
        journeyData: JourneyData,
        validator: Validator,
    ): Boolean {
        var currentStep: Step<T>? = steps.single { it.id == startingStepId }
        while (currentStep != null && currentStep in steps) {
            if (isStepComplete(journeyData, currentStep, validator)) {
                val nextStepId = currentStep.nextAction(journeyData, null).first
                currentStep = steps.singleOrNull { it.id == nextStepId }
            } else {
                return false
            }
        }
        return true
    }

    private fun isStepComplete(
        journeyData: JourneyData,
        step: Step<T>,
        validator: Validator,
    ): Boolean {
        val pageData = JourneyDataHelper.getPageData(journeyData, step.name)
        return pageData != null && step.isSatisfied(validator, pageData)
    }

    companion object {
        fun <T : StepId> withOneStep(
            onlyStep: Step<T>,
            nameKey: String? = null,
        ) = JourneyTask(onlyStep.id, setOf(onlyStep), nameKey)
    }
}
