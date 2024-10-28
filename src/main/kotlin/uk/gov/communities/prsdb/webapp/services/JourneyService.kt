package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.models.journeyModels.Journey
import uk.gov.communities.prsdb.webapp.models.journeyModels.Step
import uk.gov.communities.prsdb.webapp.models.journeyModels.StepId

@Service
class JourneyService(
    val formContextRepository: FormContextRepository,
    val oneLoginUserRepository: OneLoginUserRepository,
) {
    fun getJourneyView(
        journey: Journey<*>,
        step: StepId,
        context: Map<String, JsonElement>?,
    ): Map<String, String> {
        if (context != null) {
            validateFormContextForStep(journey, step, context)
        }
        return getView(journey.steps[step]!!, context)
    }

    private fun validateFormContextForStep(
        journey: Journey<*>,
        step: StepId,
        context: Map<String, JsonElement>?,
    ): Boolean = journey.validateFormContextForStep(step, context)

    private fun getView(
        step: Step<out StepId>,
        context: Map<String, JsonElement>?,
    ): Map<String, String> =
        step.page
            .getModelAttributes(context)

    fun updateFormContextAndGetNextStep(
        journey: Journey<*>,
        step: StepId,
        principalName: String,
        formData: Map<String, JsonElement>,
        formContextId: Long?,
        context: Map<String, JsonElement>?,
    ): Map<String, JsonElement> {
        validateFormData(journey.steps[step]!!, formData)
        return if (context != null) {
            updateFormContext(formData, principalName, context, formContextId!!, journey.steps[step]!!)
        } else {
            createFormContext(formData, journey, principalName)
        }
    }

    private fun updateFormContext(
        formData: Map<String, JsonElement>,
        principalName: String,
        context: Map<String, JsonElement>,
        formContextId: Long,
        step: Step<out StepId>,
    ): Map<String, JsonElement> {
        val formContext = formContextRepository.findById(formContextId).get()
        validateUser(principalName, formContext)
        val updatedContext = step.updateContext(context, formData)
        formContext.context = Json.encodeToString(updatedContext)
        formContextRepository.save(formContext)
        return updatedContext
    }

    private fun validateUser(
        principalName: String,
        formContext: FormContext,
    ) {
        if (principalName != formContext.id.toString()) {
            throw Exception("Should this be an unauthorized???")
        }
    }

    private fun createFormContext(
        formData: Map<String, JsonElement>,
        journey: Journey<*>,
        principalName: String,
    ): Map<String, JsonElement> {
        val user = oneLoginUserRepository.findById(principalName).get()
        val formContext = formContextRepository.save(FormContext(journey.journeyType, Json.encodeToString(formData), user))
        return getMappedData(formContext.context)
    }

    private fun validateFormData(
        step: Step<out StepId>,
        formData: Map<String, JsonElement>,
    ): Boolean =
        step
            .page
            .validateSubmission(formData)

    fun getRedirectUrl(
        journey: Journey<*>,
        step: StepId,
        context: Map<String, JsonElement>,
        id: Long?,
    ): String {
        val nextStepId =
            journey.steps
                .get(step)
                ?.nextStep
        return "redirect:/$journey/$nextStepId"
    }

    fun getMappedData(formData: String): Map<String, JsonElement> {
        val data = Json.parseToJsonElement(formData)
        require(data is JsonObject) { "Only Json Objects can be converted to a Map" }
        return data
    }
}
