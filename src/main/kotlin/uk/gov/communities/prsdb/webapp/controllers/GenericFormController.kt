package uk.gov.communities.prsdb.webapp.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpSession
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.multipageforms.Journey
import uk.gov.communities.prsdb.webapp.multipageforms.JourneyData
import uk.gov.communities.prsdb.webapp.multipageforms.Step
import uk.gov.communities.prsdb.webapp.multipageforms.StepAction
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
            val standardStepIds =
                journey.steps
                    .filter { (_, step) -> step is Step.StandardStep<*, *> }
                    .map { (id, _) -> id }
            standardStepIds.forEach { stepId ->
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
) {
    @GetMapping
    fun showMultiPageFormStep(
        @PathVariable journeyName: String,
        @PathVariable stepName: String,
        @RequestParam(required = false) entityIndex: Int?,
        model: Model,
        session: HttpSession,
    ): String {
        val journey = getJourney(journeyName)
        val stepId = getStepId(journey, stepName)

        val journeyData = session.getAttribute("journeyData") as? JourneyData ?: mutableMapOf()

        if (!journey.isReachable(journeyData, stepId)) {
            return "redirect:/$journeyName/${journey.initialStepId.urlPathSegment}"
        }

        val step = getStep(journey, stepId)
        val submittedFormData = model.getAttribute("formValues") as Map<String, String>?
        val pageModel =
            if (submittedFormData != null) {
                step.page.bindFormDataToModel(submittedFormData)
            } else {
                step.bindJourneyDataToModel(journeyData, entityIndex)
            }

        model.addAttribute("messageKeys", step.page.messageKeys)
        model.addAttribute("pageModel", pageModel)
        model.addAttribute("buttons", step.page.buttons)

        return step.page.templateName
    }

    @PostMapping(consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun handleStepSubmission(
        @PathVariable journeyName: String,
        @PathVariable stepName: String,
        @RequestParam formDataMap: Map<String, String>,
        @RequestParam(required = false) entityIndex: Int?,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
        principal: Principal,
    ): String {
        val journey = getJourney(journeyName)
        val stepId = getStepId(journey, stepName)
        val step = getStep(journey, stepId)

        // Validate the form submission
        val pageModel = step.page.bindFormDataToModel(formDataMap)
        if (pageModel.hasErrors()) {
            redirectAttributes.addFlashAttribute("formValues", formDataMap)
            return "redirect:/$journeyName/$stepName"
        }

        // Update session data with form input
        val journeyData = session.getAttribute("journeyData") as? JourneyData ?: mutableMapOf()
        step.updateJourneyData(journeyData, formDataMap, entityIndex)
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

        return when (val nextStepAction = step.nextStepAction) {
            is StepAction.GoToStep<*> -> {
                val nextStepName = nextStepAction.stepId.urlPathSegment
                "redirect:/$journeyName/$nextStepName"
            }
            is StepAction.Redirect<*> -> {
                "redirect:${nextStepAction.path}"
            }
            is StepAction.GoToOrLoop<*> -> {
                if (formDataMap["action"] == "repeat") {
                    "redirect:/$journeyName/${nextStepAction.loopId.urlPathSegment}"
                } else {
                    "redirect:/$journeyName/${nextStepAction.nextId.urlPathSegment}"
                }
            }
            is StepAction.RedirectOrLoop<*> -> {
                if (formDataMap["action"] == "repeat") {
                    "redirect:/$journeyName/${nextStepAction.loopId.urlPathSegment}"
                } else {
                    "redirect:${nextStepAction.path}"
                }
            }
        }
    }

    private fun getJourney(journeyName: String): Journey<StepId> =
        (
            journeys.find { it.journeyType.urlPathSegment.equals(journeyName, ignoreCase = true) }
                ?: throw IllegalArgumentException("Journey named \"$journeyName\" not found")
        ) as Journey<StepId>

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
    ): Step.StandardStep<*, *> = journey.steps[stepId] as Step.StandardStep<*, *>
}
