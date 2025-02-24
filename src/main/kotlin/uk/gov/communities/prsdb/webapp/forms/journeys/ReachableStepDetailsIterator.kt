package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper

class ReachableStepDetailsIterator<T : StepId>(
    val journeyData: JourneyData,
    val steps: Iterable<Step<T>>,
    val initialStepId: T,
    val validator: Validator,
) : Iterator<StepDetails<T>> {
    lateinit var currentStepDetails: StepDetails<T>

    override fun hasNext(): Boolean {
        if (!this::currentStepDetails.isInitialized) return steps.count { step -> step.id == initialStepId } == 1
        val pageData = JourneyDataHelper.getPageData(journeyData, currentStepDetails.step.name, currentStepDetails.subPageNumber)
        if (pageData == null || !currentStepDetails.step.isSatisfied(validator, pageData)) {
            return false
        }

        return currentStepDetails.step.nextAction(journeyData, currentStepDetails.subPageNumber).first != null
    }

    override fun next(): StepDetails<T> {
        if (!this::currentStepDetails.isInitialized) {
            currentStepDetails = StepDetails(steps.single { step -> step.id == initialStepId }, null, mutableMapOf())
        } else {
            val pageData = JourneyDataHelper.getPageData(journeyData, currentStepDetails.step.name, currentStepDetails.subPageNumber)
            if (pageData == null || !currentStepDetails.step.isSatisfied(validator, pageData)) {
                throw PrsdbWebException("Does not have next")
            }
            val stepData = JourneyDataHelper.getPageData(journeyData, currentStepDetails.step.name, null)
            currentStepDetails.filteredJourneyData[currentStepDetails.step.name] = stepData

            val (nextStepId, nextSubPageNumber) = currentStepDetails.step.nextAction(journeyData, currentStepDetails.subPageNumber)
            if (nextStepId == null) throw PrsdbWebException("Does not have next")
            val nextStep = steps.single { step -> step.id == nextStepId }
            currentStepDetails = StepDetails(nextStep, nextSubPageNumber, currentStepDetails.filteredJourneyData)
        }
        return currentStepDetails
    }
}
