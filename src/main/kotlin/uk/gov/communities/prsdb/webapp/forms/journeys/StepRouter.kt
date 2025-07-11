package uk.gov.communities.prsdb.webapp.forms.journeys

import uk.gov.communities.prsdb.webapp.forms.steps.GroupedStepId
import uk.gov.communities.prsdb.webapp.forms.steps.GroupedUpdateStepId
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.StepId

interface StepRouter<T : StepId> {
    fun isDestinationAllowedWhenCheckingAnswersFor(
        destinationStep: T?,
        stepBeingChecked: T?,
    ): Boolean
}

open class IsolatedStepRouter<T : StepId> : StepRouter<T> {
    override fun isDestinationAllowedWhenCheckingAnswersFor(
        destinationStep: T?,
        stepBeingChecked: T?,
    ): Boolean = destinationStep != null && destinationStep == stepBeingChecked
}

open class GroupedStepRouter<T : GroupedStepId<*>>(
    private val steps: Iterable<StepDetails<T>>,
) : StepRouter<T> {
    override fun isDestinationAllowedWhenCheckingAnswersFor(
        destinationStep: T?,
        stepBeingChecked: T?,
    ): Boolean =
        destinationStep != null &&
            destinationStep.groupIdentifier == stepBeingChecked?.groupIdentifier &&
            isDestinationNotBeforeOtherStep(destinationStep, stepBeingChecked)

    private fun isDestinationNotBeforeOtherStep(
        destinationStep: T?,
        otherStep: T?,
    ): Boolean =
        destinationStep != null &&
            steps.fold(null as Boolean?) { destinationIsAfterOtherStep, stepDetails ->
                destinationIsAfterOtherStep
                    ?: when (stepDetails.step.id) {
                        otherStep -> true
                        destinationStep -> false
                        else -> null
                    }
            } ?: false
}

open class GroupedUpdateStepRouter<T : GroupedUpdateStepId<*>>(
    steps: Iterable<StepDetails<T>>,
) : GroupedStepRouter<T>(steps) {
    override fun isDestinationAllowedWhenCheckingAnswersFor(
        destinationStep: T?,
        stepBeingChecked: T?,
    ): Boolean =
        destinationStep?.isCheckYourAnswersStepId != true &&
            super.isDestinationAllowedWhenCheckingAnswersFor(destinationStep, stepBeingChecked)
}
