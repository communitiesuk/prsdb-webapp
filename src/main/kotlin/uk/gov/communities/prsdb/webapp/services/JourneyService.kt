package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyStep
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository

@Service
class JourneyService(
    val formContextRepository: FormContextRepository,
    val oneLoginUserRepository: OneLoginUserRepository,
) {
    fun getJourneyView(
        journeyType: JourneyType,
        journeyStep: JourneyStep,
        context: Map<String, JsonElement>?,
    ): String {
        if (context != null) {
            validateFormContextForStep(journeyType, journeyStep, context)
        }
        return getView(journeyType, journeyStep)
    }

    private fun validateFormContextForStep(
        journeyType: JourneyType,
        journeyStep: JourneyStep,
        context: Map<String, JsonElement>?,
    ): Boolean = journeyType.validateFormContextForStep(journeyStep, context)

    private fun getView(
        journey: JourneyType,
        journeyStep: JourneyStep,
    ): String {
        // TODO for step get page with content and view
        // TODO get view and return
        return "index"
    }

    fun updateFormContextAndGetNextStep(
        journeyType: JourneyType,
        journeyStep: JourneyStep,
        principalName: String,
        formData: Map<String, JsonElement>,
        formContextId: Long?,
        context: Map<String, JsonElement>?,
    ): Map<String, JsonElement> {
        validateFormData(journeyStep, formData)
        return if (context != null) {
            updateFormContext(formData, principalName, context, formContextId!!, journeyStep)
        } else {
            createFormContext(formData, journeyType, principalName)
        }
    }

    private fun updateFormContext(
        formData: Map<String, JsonElement>,
        principalName: String,
        context: Map<String, JsonElement>,
        formContextId: Long,
        journeyStep: JourneyStep,
    ): Map<String, JsonElement> {
        val formContext = formContextRepository.findById(formContextId).get()
        validateUser(principalName, formContext)
        val updatedContext = journeyStep.updateContext(context, formData)
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
        journeyType: JourneyType,
        principalName: String,
    ): Map<String, JsonElement> {
        val user = oneLoginUserRepository.findById(principalName).get()
        val formContext = formContextRepository.save(FormContext(journeyType, Json.encodeToString(formData), user))
        return getMappedData(formContext.context)
    }

    private fun validateFormData(
        step: JourneyStep,
        formData: Map<String, JsonElement>?,
    ): Boolean {
        // TODO add validation process
        // TODO - if not valid throws Exception("Invalid form data")
        return true
    }

    fun getRedirectUrl(
        journey: JourneyType,
        journeyStep: JourneyStep,
        context: Map<String, JsonElement>,
        id: Long?,
    ): String {
        val nextStep =
            journey.resolveNext(
                context,
                journeyStep,
            )
        // TODO get use next step and journey to the redirect url AND add the formContextId body submission
        return "index"
    }

    fun getMappedData(formData: String): Map<String, JsonElement> {
        val data = Json.parseToJsonElement(formData)
        require(data is JsonObject) { "Only Json Objects can be converted to a Map" }
        return data
    }
}
