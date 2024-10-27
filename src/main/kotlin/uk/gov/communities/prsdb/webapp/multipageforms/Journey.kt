package uk.gov.communities.prsdb.webapp.multipageforms

import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import kotlin.reflect.KClass

data class Journey<TStepId : StepId>(
    val stepIdType: KClass<TStepId>,
    val journeyType: JourneyType,
    val initialStepId: TStepId,
    val steps: Map<TStepId, Step<*, TStepId>>,
    val isReachable: (Map<String, Any>, StepId) -> Boolean = { journeyData, stepId ->
        if (!stepIdType.isInstance(stepId)) {
            throw IllegalArgumentException(
                "Expected stepId to be of type ${stepIdType.simpleName} but found ${stepId.javaClass.simpleName}",
            )
        }
        var currentStepId: TStepId? = initialStepId
        while (currentStepId != null && currentStepId != stepId) {
            val currentStep = steps[currentStepId]!!
            if (!currentStep.isSatisfied(journeyData)) {
                break // If not satisfied, break with currentStepId not equal to stepId
            }
            currentStepId = currentStep.nextStep(journeyData)
        }
        currentStepId == stepId
    },
)
