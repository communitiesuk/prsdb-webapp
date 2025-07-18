package uk.gov.communities.prsdb.webapp.forms

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.NonStepJourneyDataKey
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper

class ReachableStepDetailsIterator<T : StepId>(
    private val journeyData: JourneyData,
    private val steps: Iterable<Step<T>>,
    private val initialStepId: T,
    private val validator: Validator,
) : Iterator<StepDetails<T>> {
    private val nonStepJourneyData =
        NonStepJourneyDataKey.entries
            .filter { journeyData.containsKey(it.key) }
            .associate { (it.key to journeyData[it.key]) }

    private lateinit var currentStepDetails: StepDetails<T>
    private var currentFilteredJourneyData: JourneyData = nonStepJourneyData

    override fun hasNext(): Boolean {
        if (!this::currentStepDetails.isInitialized) {
            return steps.count { step -> step.id == initialStepId } == 1
        }

        return isStepSatisfied(currentStepDetails) && subsequentStepDetailsOrNull(currentStepDetails) != null
    }

    override fun next(): StepDetails<T> {
        currentStepDetails =
            if (!this::currentStepDetails.isInitialized) {
                initialStepDetails()
            } else {
                subsequentStepDetails(currentStepDetails)
            }
        currentFilteredJourneyData = currentStepDetails.filteredJourneyData
        return currentStepDetails
    }

    private fun initialStepDetails() =
        steps.singleOrNull { step -> step.id == initialStepId }?.let {
            StepDetails(it, null, subsequentFilteredJourneyData(currentFilteredJourneyData, it.name))
        } ?: throw NoSuchElementException("Journey does not have initial step")

    private fun subsequentStepDetails(currentStep: StepDetails<T>): StepDetails<T> {
        if (!isStepSatisfied(currentStep)) {
            throw NoSuchElementException("The previous step of the journey has not been validly completed")
        }
        return subsequentStepDetailsOrNull(currentStep)
            ?: throw NoSuchElementException("The previous step of the journey is the last step")
    }

    private fun subsequentStepDetailsOrNull(currentStep: StepDetails<T>): StepDetails<T>? {
        val (nextStepId, nextSubPageNumber) = currentStep.step.nextAction(currentFilteredJourneyData, currentStep.subPageNumber)
        val nextStep = steps.singleOrNull { step -> step.id == nextStepId } ?: return null
        val nextFilteredJourneyData = subsequentFilteredJourneyData(currentFilteredJourneyData, nextStep.name)
        return StepDetails(nextStep, nextSubPageNumber, nextFilteredJourneyData)
    }

    private fun subsequentFilteredJourneyData(
        filteredJourneyData: JourneyData,
        stepName: String,
    ): JourneyData {
        val stepData = JourneyDataHelper.getPageData(journeyData, stepName, null)
        return filteredJourneyData + Pair(stepName, stepData)
    }

    private fun isStepSatisfied(step: StepDetails<T>): Boolean {
        val subPageData = JourneyDataHelper.getPageData(journeyData, step.step.name, step.subPageNumber)
        val bindingResult = step.step.page.bindDataToFormModel(validator, subPageData)
        return subPageData != null && step.step.isSatisfied(bindingResult)
    }
}
