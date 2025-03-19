package uk.gov.communities.prsdb.webapp.forms

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import java.util.Stack
import kotlin.NoSuchElementException

class ReachableStepDetailsIterator<T : StepId>(
    private val journeyData: JourneyData,
    private val steps: Iterable<Step<T>>,
    private val initialStepId: T,
    private val validator: Validator,
) : Iterator<StepDetails<T>> {
    private lateinit var currentStepDetails: StepDetails<T>
    private val reachableStepStack = Stack<StepDetails<T>>()
    private val visitedSteps = mutableSetOf<StepDetails<T>>()

    init {
        pushInitialStepToStack()
    }

    override fun hasNext() = reachableStepStack.isNotEmpty()

    override fun next(): StepDetails<T> =
        if (hasNext()) {
            visitNextReachableStep()
        } else {
            throw NoSuchElementException("The current step of the journey is the last step")
        }

    private fun pushInitialStepToStack() {
        val initialStepDetails = reachableStepDetails(initialStepId)
        if (initialStepDetails != null) {
            reachableStepStack.push(initialStepDetails)
        } else {
            throw NoSuchElementException("$initialStepId is not a valid initial step")
        }
    }

    private fun visitNextReachableStep(): StepDetails<T> {
        currentStepDetails = reachableStepStack.pop()
        if (currentStepDetails !in visitedSteps) {
            visitedSteps.add(currentStepDetails)
            pushReachableStepsToStack(currentStepDetails)
        }
        return currentStepDetails
    }

    private fun pushReachableStepsToStack(currentStep: StepDetails<T>) {
        if (!isStepSatisfied(currentStep)) return

        // We reverse the reachableActions so they are visited from left to right
        currentStep.step
            .reachableActions(journeyData, currentStep.subPageNumber)
            .reversed()
            .forEach { (stepId, subPageNumber) ->
                reachableStepDetails(stepId, subPageNumber)?.let { if (it !in visitedSteps) reachableStepStack.push(it) }
            }
    }

    private fun isStepSatisfied(step: StepDetails<T>): Boolean {
        val subPageData = JourneyDataHelper.getPageData(journeyData, step.step.name, step.subPageNumber) ?: emptyMap()
        return step.step.isSatisfied(validator, subPageData)
    }

    private fun reachableStepDetails(
        reachableStepId: T,
        subPageNumber: Int? = null,
    ): StepDetails<T>? {
        val reachableStep = steps.singleOrNull { step -> step.id == reachableStepId } ?: return null
        return StepDetails(reachableStep, subPageNumber, reachableStepFilteredJourneyData(reachableStep.name))
    }

    private fun reachableStepFilteredJourneyData(stepName: String): JourneyData {
        val currentFilteredJourneyData = if (::currentStepDetails.isInitialized) currentStepDetails.filteredJourneyData else emptyMap()
        val stepData = JourneyDataHelper.getPageData(journeyData, stepName, null)
        return currentFilteredJourneyData + Pair(stepName, stepData)
    }
}
