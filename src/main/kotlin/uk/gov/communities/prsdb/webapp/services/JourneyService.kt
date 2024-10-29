package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.springframework.stereotype.Service
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.models.journeyModels.Journey
import uk.gov.communities.prsdb.webapp.models.journeyModels.Step
import uk.gov.communities.prsdb.webapp.models.journeyModels.StepId

@Service
class JourneyService(
    val journeys: List<Journey<*>>,
    val formContextRepository: FormContextRepository,
    val oneLoginUserRepository: OneLoginUserRepository,
) {
    fun getJourneyView(
        journeyType: String,
        stepName: String,
        context: Map<String, Any>,
    ): ModelAndView {
        // TODO this can be one function that returns the three as needed?
        val journey = getJourney(journeyType)
        val stepId = getStepId(journey, stepName)
        val step = getStep(journey, stepId)
        if (context.isNotEmpty()) {
            validateFormContextForStep(journey, stepId, context)
        }
        return getView(step, context)
    }

    private fun validateFormContextForStep(
        journey: Journey<*>,
        stepId: StepId,
        context: Map<String, Any>,
        // TODO this should return either a success or error
    ): Boolean = journey.validateFormContextForStep(context, stepId)

    private fun getView(
        step: Step<out StepId>,
        context: Map<String, Any>,
    ): ModelAndView {
        val pageFields: Map<String, String> = step.getSubmissionFromFormContext(context)
        return step.page
            .getModelAttributes(pageFields)
    }

    fun updateFormContextAndGetNextStep(
        journeyType: String,
        stepName: String,
        principalName: String,
        formData: Map<String, String>,
        formContextId: Long?,
        context: Map<String, Any>,
    ): Map<String, Any> {
        val journey = getJourney(journeyType)
        val stepId = getStepId(journey, stepName)
        val step = getStep(journey, stepId)

        validateFormData(step, formData)
        return if (context.isNotEmpty()) {
            updateFormContext(formData, principalName, context, formContextId!!, step)
        } else {
            createFormContext(formData, journey, principalName)
        }
    }

    private fun updateFormContext(
        formData: Map<String, String>,
        principalName: String,
        context: Map<String, Any>,
        formContextId: Long,
        step: Step<out StepId>,
    ): Map<String, Any> {
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
        formData: Map<String, String>,
        journey: Journey<*>,
        principalName: String,
    ): Map<String, Any> {
        val user = oneLoginUserRepository.findById(principalName).get()
        val formContext = formContextRepository.save(FormContext(journey.journeyType, Json.encodeToString(formData), user))
        return getMappedData(formContext.context)
    }

    private fun validateFormData(
        step: Step<out StepId>,
        formData: Map<String, String>,
    ): Boolean =
        step
            .page
            .validateSubmission(formData)

    fun getRedirectUrl(
        journeyType: String,
        stepName: String,
        context: Map<String, Any>,
        id: Long?,
    ): String {
        val journey = getJourney(journeyType)
        val stepId = getStepId(journey, stepName)
        val step = getStep(journey, stepId)
        val nextStepId = step.nextStep
        return "redirect:/$journey/$nextStepId"
    }

    fun getMappedData(formData: String): Map<String, JsonElement> {
        val data = Json.parseToJsonElement(formData)
        require(data is JsonObject) { "Only Json Objects can be converted to a Map" }
        return data
    }

    private fun getJourney(journeyName: String): Journey<*> =
        journeys.find { it.journeyType.urlPathSegment.equals(journeyName, ignoreCase = true) }
            ?: throw IllegalArgumentException("Journey named \"$journeyName\" not found")

    private fun getStepId(
        journey: Journey<*>,
        stepName: String,
    ): StepId {
        val stepIds = journey.steps.keys
        return stepIds.find { it.urlPathSegment.equals(stepName, ignoreCase = true) }
            ?: throw IllegalArgumentException("No step named \"$stepName\" found in journey \"${journey.journeyType.name}\"")
    }

    private fun getStep(
        journey: Journey<*>,
        stepId: StepId,
    ): Step<*> = journey.steps[stepId]!!
}
