package uk.gov.communities.prsdb.webapp.models.journeyModels

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
            currentStepId = nextStep
        }
        currentStepId == targetStepId
    },
)

class JourneyBuilder<TStepId : StepId> {
    lateinit var journeyType: JourneyType
    lateinit var initialStepId: StepId
    lateinit var steps: Map<TStepId, Step<TStepId>>
    lateinit var validateFormContextForNextstep: (Map<String, Any>, StepId) -> Boolean

    fun build(): Journey<TStepId> = Journey(journeyType, initialStepId, steps, validateFormContextForNextstep)
}
