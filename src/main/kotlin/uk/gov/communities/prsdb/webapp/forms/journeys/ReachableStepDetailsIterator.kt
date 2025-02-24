package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper

class ReachableStepDetailsIterator<T : StepId>(
    journeyData: JourneyData,
    private val steps: Iterable<Step<T>>,
    private val initialStepId: T,
    private val validator: Validator,
) : Iterator<StepDetails<T>> {
    private lateinit var currentStepDetails: StepDetails<T>
    private var currentFilteredJourneyData: Map<String, Any?> = mapOf()
    private val immutableJourneyData = journeyData.toMap()

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
        return currentStepDetails
    }

    private fun initialStepDetails() =
        steps.singleOrNull { step -> step.id == initialStepId }.let {
            if (it == null) {
                throw NoSuchElementException("Journey does not have initial step")
            } else {
                StepDetails(it, null, currentFilteredJourneyData.toMutableMap())
            }
        }

    private fun getInitialStepDetailsProcedural(): StepDetails<T> {
        val initialStepOrNull = steps.singleOrNull { step -> step.id == initialStepId }
        if (initialStepOrNull == null) {
            throw NoSuchElementException("Journey does not have initial step")
        }

        return StepDetails(initialStepOrNull, null, mutableMapOf())
    }

    private fun subsequentStepDetails(currentStep: StepDetails<T>): StepDetails<T> {
        if (!isStepSatisfied(currentStep)) {
            throw NoSuchElementException("The previous step of the journey has not been validly completed")
        }
        return subsequentStepDetailsOrNull(currentStep)
            ?: throw NoSuchElementException("The previous step of the journey is the last step")
    }

    private fun subsequentStepDetailsOrNull(currentStep: StepDetails<T>): StepDetails<T>? {
        currentFilteredJourneyData = subsequentFilteredJourneyData(currentFilteredJourneyData)

        val (nextStepId, nextSubPageNumber) =
            currentStep.step.nextAction(
                immutableJourneyData.toMutableMap(),
                currentStep.subPageNumber,
            )
        val nextStep = steps.singleOrNull { step -> step.id == nextStepId }

        if (nextStep == null) {
            return null
        }
        return StepDetails(nextStep, nextSubPageNumber, currentFilteredJourneyData.toMutableMap())
    }

    private fun subsequentFilteredJourneyData(filteredJourneyData: Map<String, Any?>): Map<String, Any?> {
        val stepData =
            JourneyDataHelper.getPageData(
                immutableJourneyData.toMutableMap(),
                currentStepDetails.step.name,
                null,
            )
        return filteredJourneyData + Pair(currentStepDetails.step.name, stepData)
    }

    private fun isStepSatisfied(step: StepDetails<T>): Boolean {
        val subPageData =
            JourneyDataHelper.getPageData(
                immutableJourneyData.toMutableMap(),
                step.step.name,
                step.subPageNumber,
            )
        return subPageData != null && step.step.isSatisfied(validator, subPageData)
    }
}
