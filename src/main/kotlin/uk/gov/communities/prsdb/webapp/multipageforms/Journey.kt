package uk.gov.communities.prsdb.webapp.multipageforms

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType

/**
 * A Journey is a definition of a flow that a user can take to complete a multi-page form
 */
data class Journey<TStepId : StepId>(
    val journeyType: JourneyType,
    val initialStepId: TStepId,
    val steps: Map<TStepId, Step<TStepId>>,
    val isReachable: (JourneyData, TStepId) -> Boolean = { journeyData, stepId ->
        var currentStepId = initialStepId
        while (currentStepId != stepId) {
            val currentStep = steps[currentStepId]!!
            if (!currentStep.isSatisfied(journeyData)) {
                break // If not satisfied, break with currentStepId not equal to stepId
            }
            currentStepId =
                when (val nextAction = currentStep.nextStepAction) {
                    is StepAction.GoToStep<TStepId> -> {
                        nextAction.stepId
                    }
                    is StepAction.Redirect -> {
                        break // We've reached a terminal step
                    }
                    is StepAction.GoToOrLoop -> nextAction.nextId
                    is StepAction.RedirectOrLoop -> {
                        break // We've reach a terminal step
                    }
                }
        }
        currentStepId == stepId
    },
)

class JourneyBuilder<TStepId : StepId>(
    private val validator: Validator,
) {
    lateinit var journeyType: JourneyType
    lateinit var initialStepId: TStepId
    private val steps = mutableMapOf<TStepId, Step<TStepId>>()

    fun step(
        stepId: TStepId,
        init: StepBuilder<TStepId>.() -> Unit,
    ) {
        steps[stepId] = StepBuilder(validator, stepId).apply(init).build()
    }

    fun interstitial(
        stepId: TStepId,
        nextStepId: TStepId,
    ) {
        steps[stepId] = Step.InterstitialStep(StepAction.GoToStep(nextStepId))
    }

    fun build(): Journey<TStepId> = Journey(journeyType, initialStepId, steps)
}

fun <TStepId : StepId> journey(
    validator: Validator,
    init: JourneyBuilder<TStepId>.() -> Unit,
): Journey<TStepId> = JourneyBuilder<TStepId>(validator).apply(init).build()
