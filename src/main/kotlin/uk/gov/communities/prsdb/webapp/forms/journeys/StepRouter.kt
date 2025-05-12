package uk.gov.communities.prsdb.webapp.forms.journeys

import uk.gov.communities.prsdb.webapp.forms.steps.GroupedStepId
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.StepId

interface StepRouter<T : StepId> {
    fun isDestinationAllowedWhenChangingAnswerTo(
        destinationStep: T?,
        stepBeingChanged: T?,
    ): Boolean
}

open class IsolatedStepRouter<T : StepId> : StepRouter<T> {
    override fun isDestinationAllowedWhenChangingAnswerTo(
        destinationStep: T?,
        stepBeingChanged: T?,
    ): Boolean = destinationStep != null && destinationStep == stepBeingChanged
}

open class GroupedStepRouter<T : GroupedStepId<*>>(
    private val steps: Iterable<StepDetails<T>>,
) : StepRouter<T> {
    override fun isDestinationAllowedWhenChangingAnswerTo(
        destinationStep: T?,
        stepBeingChanged: T?,
    ): Boolean =
        destinationStep != null &&
            destinationStep.groupIdentifier == stepBeingChanged?.groupIdentifier &&
            isDestinationNotBeforeOtherStep(destinationStep, stepBeingChanged)

    private fun isDestinationNotBeforeOtherStep(
        destinationStep: T?,
        otherStep: T?,
    ): Boolean =
        destinationStep != null &&
            steps.fold(null) { destinationIsAfterOtherStep, stepDetails ->
                destinationIsAfterOtherStep
                    ?: when (stepDetails.step.id) {
                        otherStep -> true
                        destinationStep -> false
                        else -> destinationIsAfterOtherStep
                    }
            } ?: false
}
