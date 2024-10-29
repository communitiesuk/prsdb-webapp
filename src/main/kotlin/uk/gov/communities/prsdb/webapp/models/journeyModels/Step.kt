package uk.gov.communities.prsdb.webapp.models.journeyModels

import kotlinx.serialization.json.JsonElement

data class Step<TStepId : StepId>(
    val page: Page,
    val nextStep: (Map<String, Any>) -> StepId,
    val getSubmissionFromFormContext: (Map<String, String>) -> Map<String, String>,
) {
    fun updateContext(
        context: Map<String, JsonElement>,
        formData: Map<String, JsonElement>,
    ): Map<String, JsonElement> {
        // This is the default strategy to adding the new data but will need to be overridden for more complex data structures
        // TODO add an example of the above?
        return context + formData
    }
}
