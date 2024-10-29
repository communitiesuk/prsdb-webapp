package uk.gov.communities.prsdb.webapp.models.journeyModels

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType

data class Journey<TStepId : StepId>(
    val journeyType: JourneyType,
    val initialStepId: StepId,
    val steps: Map<TStepId, Step<TStepId>>,
    val validateFormContextForStep: (Map<String, Any>, StepId) -> Boolean = { formContext, targetStepId ->
        var currentStepId = initialStepId
        while (currentStepId != targetStepId) {
            val currentStep = steps[currentStepId]!!
            if (!currentStep.isSatisfied(formContext)) {
                break
            }
            val nextStep = currentStep.nextStep(formContext)
            // TODO-PRSD-422 if nextStep == targetStepId then store currentStep as backTarget (to populate the back route)
            currentStepId = nextStep
        }
        currentStepId == targetStepId
    },
)

class JourneyBuilder<TStepId : StepId>(
    validator: Validator,
) {
    lateinit var journeyType: JourneyType
    lateinit var initialStepId: StepId
    private var steps = mutableMapOf<TStepId, Step<TStepId>>()
    lateinit var validateFormContextForNextstep: (Map<String, Any>, StepId) -> Boolean

    fun step(
        stepId: TStepId,
        init: StepBuilder<TStepId>.() -> Unit,
    ) {
        steps[stepId] = StepBuilder<TStepId>().apply(init).build()
    }

    fun build(): Journey<TStepId> = Journey(journeyType, initialStepId, steps, validateFormContextForNextstep)
}

fun <TStepId : StepId> journey(
    validator: Validator,
    init: JourneyBuilder<TStepId>.() -> Unit,
): Journey<TStepId> = JourneyBuilder<TStepId>(validator).apply(init).build()
