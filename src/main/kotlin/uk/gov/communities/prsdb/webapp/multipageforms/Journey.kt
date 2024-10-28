package uk.gov.communities.prsdb.webapp.multipageforms

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType

data class Journey<TStepId : StepId>(
    val journeyType: JourneyType,
    val initialStepId: TStepId,
    val steps: Map<TStepId, Step<TStepId>>,
    val isReachable: (Map<String, Any>, TStepId) -> Boolean = { journeyData, stepId ->
        var currentStepId = initialStepId
        while (currentStepId != stepId) {
            val currentStep = steps[currentStepId]!!
            if (!currentStep.isSatisfied(journeyData)) {
                break // If not satisfied, break with currentStepId not equal to stepId
            }
            when (val nextAction = currentStep.nextStep(journeyData)) {
                is StepAction.GoToStep<TStepId> -> {
                    // TODO: Can we have nicer generics to avoid this cast?
                    currentStepId = nextAction.stepId
                }

                is StepAction.Redirect -> {
                    break // We've reached a terminal step
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
        steps[stepId] = StepBuilder<TStepId>(validator).apply(init).build()
    }

    fun build(): Journey<TStepId> = Journey(journeyType, initialStepId, steps)
}

fun <TStepId : StepId> journey(
    validator: Validator,
    init: JourneyBuilder<TStepId>.() -> Unit,
): Journey<TStepId> = JourneyBuilder<TStepId>(validator).apply(init).build()
