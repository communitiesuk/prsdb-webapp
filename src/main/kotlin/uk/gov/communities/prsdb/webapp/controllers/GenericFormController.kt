package uk.gov.communities.prsdb.webapp.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpSession
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.Validator
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.multipageforms.Journey
import uk.gov.communities.prsdb.webapp.multipageforms.Step
import uk.gov.communities.prsdb.webapp.multipageforms.StepId
import java.security.Principal
import kotlin.reflect.jvm.javaMethod

@Configuration
class JourneyPathRegistrar(
    private val journeyDefinitions: List<Journey<*>>,
    private val handlerMapping: RequestMappingHandlerMapping,
    private val genericFormController: GenericFormController,
) {
    @PostConstruct
    fun registerJourneyPaths() {
        val getMethod = GenericFormController::showMultiPageFormStep.javaMethod!!
        val postMethod = GenericFormController::handleStepSubmission.javaMethod!!

        // Register each path for every journey and step combination
        journeyDefinitions.forEach { journey ->
            journey.steps.keys.forEach { stepId ->
                val path =
                    "/{journeyName:${Regex.escape(journey.journeyType.urlPathSegment)}}/{stepName:${Regex.escape(stepId.urlPathSegment)}}"

                handlerMapping.registerMapping(
                    org.springframework.web.servlet.mvc.method.RequestMappingInfo
                        .paths(path)
                        .methods(RequestMethod.GET)
                        .build(),
                    genericFormController,
                    getMethod,
                )
                handlerMapping.registerMapping(
                    org.springframework.web.servlet.mvc.method.RequestMappingInfo
                        .paths(path)
                        .methods(RequestMethod.POST)
                        .build(),
                    genericFormController,
                    postMethod,
                )
            }
        }
    }
}

@Controller
@RequestMapping // URLs are mapped by JourneyPathRegistrar
class GenericFormController(
    private val journeys: List<Journey<*>>,
    private val formContextRepository: FormContextRepository,
    private val oneLoginUserRepository: OneLoginUserRepository,
    private val objectMapper: ObjectMapper,
    private val validator: Validator,
) {
    @GetMapping
    fun showMultiPageFormStep(
        @PathVariable journeyName: String,
        @PathVariable stepName: String,
        model: Model,
        session: HttpSession,
    ): String {
        val journey = getJourney(journeyName)
        val stepId = getStepId(journey, stepName)

        val journeyData = session.getAttribute("journeyData") as? Map<String, Any> ?: emptyMap()

        if (!journey.isReachable(journeyData, stepId)) {
            return "redirect:/$journeyName/${journey.initialStepId.urlPathSegment}"
        }

        val step = getStep(journey, stepId)
        val formData = step.page.prepopulateForm(journeyData)

        model.addAttribute("form", formData)
        model.addAttribute("messageKeys", step.page.messageKeys)

        return step.page.templateName
    }

    @PostMapping(consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun handleStepSubmission(
        @PathVariable journeyName: String,
        @PathVariable stepName: String,
        @ModelAttribute formDataMap: HashMap<String, Any>,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
        principal: Principal,
    ): String {
        val journey = getJourney(journeyName)
        val stepId = getStepId(journey, stepName)
        val step = getStep(journey, stepId)

        // Validate the form submission
        val errors = step.page.validateSubmission(formDataMap, validator)
        if (errors.isNotEmpty()) {
            redirectAttributes.addFlashAttribute("errors", errors)
            return "redirect:/$journeyName/$stepName"
        }

        // Update session data with form input
        val journeyData = session.getAttribute("journeyData") as? MutableMap<String, Any> ?: mutableMapOf()
        step.page.updateJourneyData(journeyData, formDataMap)
        session.setAttribute("journeyData", journeyData)

        // If required, update the database with the latest journey data
        if (step.persistAfterSubmit) {
            val contextId = session.getAttribute("contextId") as? Long
            if (contextId != null) {
                // Update existing FormContext
                val formContext =
                    formContextRepository
                        .findById(contextId)
                        .orElseThrow { IllegalStateException("FormContext with ID $contextId not found") }!!
                formContext.context = objectMapper.writeValueAsString(journeyData)
                formContextRepository.save(formContext)
            } else {
                // Create a new FormContext if one does not exist
                val formContext =
                    FormContext(
                        journeyType = JourneyType.valueOf(journeyName),
                        context = objectMapper.writeValueAsString(journeyData),
                        user = oneLoginUserRepository.getReferenceById(principal.name),
                    )
                formContextRepository.save(formContext)
                session.setAttribute("contextId", formContext.id)
            }
        }

        // Determine the next step using the dynamic nextStep logic
        val nextStepId =
            step.nextStep(journeyData)?.urlPathSegment
                ?: throw UnsupportedOperationException("Calculating the next step from $stepName returned null")

        return "redirect:/$journeyName/$nextStepId"
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
    ): Step<*, *> = journey.steps[stepId]!!
}
