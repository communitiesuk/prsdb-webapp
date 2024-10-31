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
    val isReachable: (JourneyData, TStepId) -> Boolean = { journeyData, targetStepId ->
        isReachable(journeyData, targetStepId, initialStepId, steps)
    },
)

fun <TStepId : StepId> isReachable(
    journeyData: JourneyData,
    targetStepId: StepId,
    initialStepId: TStepId,
    steps: Map<TStepId, Step<TStepId>>,
): Boolean {
    val visited = mutableSetOf<TStepId>()
    val queue = ArrayDeque<TStepId>()
    queue.add(initialStepId)

    while (queue.isNotEmpty()) {
        val currentStepId = queue.removeFirst()

        // If we've already visited this step, skip it
        if (!visited.add(currentStepId)) continue

        // If we've reached the target step, return true
        if (currentStepId == targetStepId) return true

        // Add all possible next steps to the queue
        val currentStep = steps[currentStepId]!!
        if (!currentStep.isSatisfied(journeyData)) {
            // If the step is not satisfied, we cannot traverse to the next step
            continue
        }
        for (action in currentStep.nextStepActions) {
            val canTraverse =
                when (action) {
                    is StepAction.Unconditional -> true
                    is StepAction.UserActionCondition -> true
                    is StepAction.SavedFormsCondition -> {
                        val savedForms = journeyData[currentStepId.urlPathSegment] ?: listOf()
                        action.condition(savedForms)
                    }
                }
            if (canTraverse) {
                val nextStepId =
                    when (action.target) {
                        is StepActionTarget.Step -> action.target.stepId
                        else -> continue
                    }
                queue.add(nextStepId)
            }
        }
    }

    // If we've traversed the whole possible graph but not found the target step, it's not reachable
    return false
}

class JourneyBuilder<TStepId : StepId>(
    private val validator: Validator,
) {
    lateinit var journeyType: JourneyType
    lateinit var initialStepId: TStepId
    private val steps = mutableMapOf<TStepId, Step<TStepId>>()

    fun step(
        stepId: TStepId,
        init: StandardStepBuilder<TStepId>.() -> Unit,
    ) {
        steps[stepId] = StandardStepBuilder(validator, stepId).apply(init).build()
    }

    fun interstitial(
        stepId: TStepId,
        init: InterstitialStepBuilder<TStepId>.() -> Unit,
    ) {
        steps[stepId] = InterstitialStepBuilder<TStepId>().apply(init).build()
    }

    fun build(): Journey<TStepId> = Journey(journeyType, initialStepId, steps)
}

fun <TStepId : StepId> journey(
    validator: Validator,
    init: JourneyBuilder<TStepId>.() -> Unit,
): Journey<TStepId> = JourneyBuilder<TStepId>(validator).apply(init).build()
