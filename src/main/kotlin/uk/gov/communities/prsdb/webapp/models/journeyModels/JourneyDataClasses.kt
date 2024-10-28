package uk.gov.communities.prsdb.webapp.models.journeyModels

import kotlinx.serialization.json.JsonElement
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType

// TODO This could be an enumInterface OR an unsealed class
sealed class StepId(
    val urlPathSegment: String,
)

// TODO This could be an enum OR an unsealed class
sealed class LandlordRegistrationStepId(
    urlPathSegment: String,
) : StepId(urlPathSegment) {
    data object Start : LandlordRegistrationStepId("start")

    data object End : LandlordRegistrationStepId("end")
}

data class Step<TStepId : StepId>(
    val page: Page,
    val nextStep: (Map<String, Any>) -> StepId,
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

// TODO this should maybe take type `subclass of form model`
class Page(
    val messageKeys: Map<String, String>,
    val formModel: FormModel,
) {
    fun getModelAttributes(formContext: Map<String, JsonElement>?): Map<String, String> {
        // TODO combines message keys, with form model AND existing context to get model attributes
        return messageKeys
    }

    fun validateSubmission(formContext: Map<String, JsonElement>): Boolean {
        // TODO based on formfields should validate the data the user submits using Spring validation
        return true
    }
}

// TODO nothing - keep it as is
data class Journey<TStepId : StepId>(
    val journeyType: JourneyType,
    val initialStepId: TStepId,
    val steps: Map<TStepId, Step<TStepId>>,
    // TODO add isReachable/getStep logic
) {
    fun validateFormContextForStep(
        step: StepId,
        context: Map<String, JsonElement>?,
    ): Boolean {
        TODO("Not yet implemented")
    }

    fun getStep(step: StepId) {
        TODO("This will return the step/page data that will populate the model attribute")
    }
}

class FormModel {
// TODO Create a FormModelClass it:
// Knows what the fields on the model are
// Can produce a map of their values/attributes
// And maybe the template names (that might be on the page)
// - INCLUDES everything specific to the data page
}

// TODO check notes in forms controller
