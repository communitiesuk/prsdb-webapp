package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.multipageforms.FormData
import uk.gov.communities.prsdb.webapp.multipageforms.Journey
import uk.gov.communities.prsdb.webapp.multipageforms.JourneyData
import uk.gov.communities.prsdb.webapp.multipageforms.Step
import uk.gov.communities.prsdb.webapp.multipageforms.StepAction
import uk.gov.communities.prsdb.webapp.multipageforms.StepActionTarget
import uk.gov.communities.prsdb.webapp.multipageforms.StepId

@Service
class MultiPageFormJourneyService(
    private val journeys: List<Journey<*>>,
) {
    fun getJourneyAndStandardStep(
        journeyName: String,
        stepName: String,
    ): Pair<Journey<StepId>, Step.StandardStep<StepId, *>> {
        val genericJourney =
            journeys.find { it.journeyType.urlPathSegment.equals(journeyName, ignoreCase = true) }
                ?: throw IllegalArgumentException("Journey named \"$journeyName\" not found")

        @Suppress("UNCHECKED_CAST")
        val journey = genericJourney as Journey<StepId>

        val stepIds = journey.steps.keys
        val stepId =
            stepIds.find { it.urlPathSegment.equals(stepName, ignoreCase = true) }
                ?: throw IllegalArgumentException("No step named \"$stepName\" found in journey \"$journeyName\"")

        val step = journey.steps[stepId]!!
        val standardStep =
            step as? Step.StandardStep<StepId, *>
                ?: throw IllegalArgumentException("Expected step named \"$stepName\" in journey \"$journeyName\" to be a StandardStep")

        return Pair(journey, standardStep)
    }

    fun resolveNextStepRedirect(
        step: Step.StandardStep<*, *>,
        formData: FormData,
        journeyData: JourneyData,
        journeyName: String,
    ): String {
        val nextAction =
            step.nextStepActions.first {
                when (it) {
                    is StepAction.Unconditional -> true
                    is StepAction.UserActionCondition -> it.condition(formData["__user-action__"]!!)
                    is StepAction.SavedFormsCondition ->
                        it.condition(
                            journeyData[it.target.stepId.urlPathSegment] ?: emptyList(),
                        )
                }
            }
        return when (nextAction.target) {
            is StepActionTarget.Step -> "/$journeyName/${nextAction.target.stepId.urlPathSegment}"
            is StepActionTarget.Path -> nextAction.target.path
        }
    }
}
