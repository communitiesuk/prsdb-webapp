package uk.gov.communities.prsdb.webapp.multipageforms

import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import kotlin.reflect.KClass
import kotlin.reflect.cast

data class Journey<TStepId : StepId>(
    val stepIdType: KClass<TStepId>,
    val journeyType: JourneyType,
    val initialStepId: TStepId,
    val steps: Map<TStepId, Step<TStepId>>,
    val isReachable: (Map<String, Any>, StepId) -> Boolean = { journeyData, stepId ->
        if (!stepIdType.isInstance(stepId)) {
            throw IllegalArgumentException(
                "Expected stepId to be of type ${stepIdType.simpleName} but found ${stepId.javaClass.simpleName}",
            )
        }
        var currentStepId: TStepId = initialStepId
        while (currentStepId != stepId) {
            val currentStep = steps[currentStepId]!!
            if (!currentStep.isSatisfied(journeyData)) {
                break // If not satisfied, break with currentStepId not equal to stepId
            }
            when (val nextAction = currentStep.nextStep(journeyData)) {
                is StepAction.GoToStep -> {
                    // TODO: Can we have nicer generics to avoid this cast?
                    currentStepId = stepIdType.cast(nextAction.stepId)
                }

                is StepAction.Redirect -> {
                    break // We've reached a terminal step
                }
            }
        }
        currentStepId == stepId
    },
)

class JourneyBuilder<TStepId : StepId> {
    lateinit var stepIdType: KClass<TStepId>
    lateinit var journeyType: JourneyType
    lateinit var initialStepId: TStepId
    private val steps = mutableMapOf<TStepId, Step<TStepId>>()

    fun step(
        stepId: TStepId,
        init: StepBuilder<TStepId>.() -> Unit,
    ) {
        steps[stepId] = StepBuilder<TStepId>().apply(init).build()
    }

    fun build() = Journey(stepIdType, journeyType, initialStepId, steps)
}

fun <TStepId : StepId> journey(init: JourneyBuilder<TStepId>.() -> Unit): Journey<TStepId> = JourneyBuilder<TStepId>().apply(init).build()
