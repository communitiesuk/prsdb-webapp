package uk.gov.communities.prsdb.webapp.controllers

import jakarta.annotation.PostConstruct
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
import uk.gov.communities.prsdb.webapp.multipageforms.FormData
import uk.gov.communities.prsdb.webapp.multipageforms.Journey
import uk.gov.communities.prsdb.webapp.multipageforms.Step
import uk.gov.communities.prsdb.webapp.services.FormContextService
import uk.gov.communities.prsdb.webapp.services.MultiPageFormJourneyService
import uk.gov.communities.prsdb.webapp.services.MultiPageFormSessionService
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

private const val SUBMITTED_FORM_DATA_FLASH_NAME = "__submittedFormData__"

@Controller
@RequestMapping // URLs are mapped by JourneyPathRegistrar
class GenericFormController(
    private val sessionService: MultiPageFormSessionService,
    private val journeyService: MultiPageFormJourneyService,
    private val formContextService: FormContextService,
) {
    @GetMapping
    fun showMultiPageFormStep(
        @PathVariable journeyName: String,
        @PathVariable stepName: String,
        @RequestParam(required = false) entityIndex: Int?,
        model: Model,
    ): String {
        val (journey, step) = journeyService.getJourneyAndStandardStep(journeyName, stepName)

        val journeyData = sessionService.getJourneyData()

        if (!journey.isReachable(journeyData, step.stepId)) {
            return "redirect:/$journeyName/${journey.initialStepId.urlPathSegment}"
        }

        @Suppress("UNCHECKED_CAST")
        val submittedFormData = model.getAttribute(SUBMITTED_FORM_DATA_FLASH_NAME) as FormData?
        val formData = submittedFormData ?: step.getFormDataOrNull(journeyData, entityIndex)
        val pageModel = step.page.bindFormDataToModel(formData)

        model.addAttribute("messageKeys", step.page.messageKeys)
        model.addAttribute("pageModel", pageModel)
        model.addAttribute("buttons", step.page.buttons)

        return step.page.templateName
    }

    @PostMapping(consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun handleStepSubmission(
        @PathVariable journeyName: String,
        @PathVariable stepName: String,
        @RequestParam formData: FormData,
        @RequestParam(required = false) entityIndex: Int?,
        redirectAttributes: RedirectAttributes,
        principal: Principal,
    ): String {
        val (journey, step) = journeyService.getJourneyAndStandardStep(journeyName, stepName)

        // Validate the form submission
        val pageModel = step.page.bindFormDataToModel(formData)
        if (pageModel.hasErrors()) {
            redirectAttributes.addFlashAttribute(SUBMITTED_FORM_DATA_FLASH_NAME, formData)
            return "redirect:/$journeyName/$stepName"
        }

        // Update session data with form input
        val journeyData = sessionService.getJourneyData()
        step.updateJourneyData(journeyData, formData, entityIndex)
        sessionService.setJourneyData(journeyData)

        // If required, update the database with the latest journey data
        if (step.persistAfterSubmit) {
            val contextIdOrNull = sessionService.getContextId()
            val contextId = formContextService.saveFormContext(contextIdOrNull, journeyData, journey, principal)
            sessionService.setContextId(contextId)
        }

        val redirectPath = journeyService.resolveNextStepRedirect(step, formData, journeyData, journeyName)
        return "redirect:$redirectPath"
    }
}
