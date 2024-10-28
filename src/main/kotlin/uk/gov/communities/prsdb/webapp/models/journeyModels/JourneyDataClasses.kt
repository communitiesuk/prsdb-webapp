package uk.gov.communities.prsdb.webapp.models.journeyModels

// TODO This could be an enumInterface OR an unsealed class
sealed class StepId(
    val name: String,
)

// TODO This could be an enum OR an unsealed class
sealed class LandlordRegistrationStepId(
    name: String,
) : StepId(name) {
    data object Start : LandlordRegistrationStepId("start")
}

data class Step<T : Any>(
    val page: Page,
    val nextStep: (T, Map<String, Any>) -> StepId,
    val isSatisfied: (Map<String, Any>) -> Boolean = {
        // TODO this should check all required fields/values for step are present in context it is the same as the validation in journeyService.ValidateFormContextFromStep
        true
    },
)

// TODO this should maybe take type `subclass of form model`
interface Page<T : Any> {
    val formContext: Map<String, String>
    val contentKeys: Map<String, String>

    // TODO the page should return a map of the initial values (null or if user has input already) AND attributes from the FormModelClass
    // TODO should return the above COMBINED with the message keys to the controller so that it can build the model

    // TODO this should either have a form model or get one
    fun validateSubmission(formContext: Map<String, String>): Boolean {
        // TODO based on formfields should validate the data the user submits
        return true
    }

    fun getTemplate(contentKeys: Map<String, String>) {
        // TODO this will return either the template or the view model
    }
}

// TODO nothing - keep it as is
data class Journey<T : StepId>(
    val id: String,
    val initialStepId: T,
    val steps: Map<T, Step<*>>,
    val isReachable: (Map<String, Any>, T) -> Boolean = { _, _ -> true },
)

// TODO Create a FormModelClass it:
// Knows what the fields on the model are
// Can produce a map of their values/attributes
// And maybe the template names (that might be on the page)
// - INCLUDES everything specific to the data page

// TODO check notes in forms controller
