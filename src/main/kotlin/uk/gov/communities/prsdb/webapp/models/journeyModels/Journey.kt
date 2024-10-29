package uk.gov.communities.prsdb.webapp.models.journeyModels

import kotlinx.serialization.json.JsonElement
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType

data class Journey<TStepId : StepId>(
    val journeyType: JourneyType,
    val initialStepId: TStepId,
    val steps: Map<TStepId, Step<TStepId>>,
) {
    fun validateFormContextForStep(
        step: StepId,
        context: Map<String, JsonElement>?,
    ): Boolean {
        // starts at the first step
        // val stepSubmission = step.getSubmissionFromFormContext(context)
        // step.page.validateSubmission(stepSubmission)
        // go to next step
        // repeat
        // until reaching current step
        // return either a success or error
        TODO("Not yet implemented")
    }
}
