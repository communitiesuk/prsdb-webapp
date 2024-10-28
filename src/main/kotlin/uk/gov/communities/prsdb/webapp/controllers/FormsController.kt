package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.config.JourneyConfig
import uk.gov.communities.prsdb.webapp.models.journeyModels.Journey
import uk.gov.communities.prsdb.webapp.models.journeyModels.StepId
import uk.gov.communities.prsdb.webapp.services.JourneyService
import java.security.Principal

@Controller
@RequestMapping("/forms")
class FormsController(
    var session: HttpSession,
    private val journeys: List<Journey<*>>,
    private val journeyService: JourneyService,
    private val journeyConfig: JourneyConfig,
) {
    @GetMapping("/{journeyName}/{stepName}")
    fun getForm(
        @PathVariable("journeyName") journeyName: String,
        @PathVariable("stepName") stepName: String,
        model: Model,
        session: HttpSession,
    ): String {
        val journey = getJourney(journeyName)
        val stepId = getStepId(journey, stepName)
        // TODO dependency eject model in controller
        // TODO once Page has returned all attributes, loop over them to add to model attributes before returning view
        val context: String? = session.getAttribute("FORM_CONTEXT")?.toString()
        val views: Map<String, String> =
            journeyService.getJourneyView(
                journey,
                stepId,
                context?.let { journeyService.getMappedData(it) },
            )

        model.addAttribute("contentHeader", "Welcome to the Private Rental Sector Database")
        model.addAttribute("title", views.getValue("title"))
        model.addAttribute("serviceName", views.getValue("serviceName"))
        model.addAttribute("postURI", "$journeyName/$stepName")
        return views.getValue("template")
    }

    @PostMapping("/{journeyName}/{stepName}")
    fun postForms(
        @PathVariable("journeyName") journeyName: String,
        @PathVariable("stepName") stepName: String,
        body: PostSubmission,
        principal: Principal,
    ): String {
        val journey = getJourney(journeyName)
        val stepId = getStepId(journey, stepName)
        val context: String? = session.getAttribute("FORM_CONTEXT")?.toString()
        val formContextId: Long? = session.getAttribute("FORM_CONTEXT_ID")?.toString()?.toLongOrNull()
        val updatedContext: Map<String, JsonElement> =
            journeyService.updateFormContextAndGetNextStep(
                journey,
                stepId,
                principal.name,
                journeyService.getMappedData(body.formData),
                formContextId,
                context?.let { journeyService.getMappedData(it) },
            )

        session.setAttribute("FORM_CONTEXT", Json.encodeToString(updatedContext))
        session.setAttribute("FORM_CONTEXT_ID", formContextId)

        return journeyService.getRedirectUrl(
            journey,
            stepId,
            updatedContext,
            formContextId,
        )
    }

    class PostSubmission(
        val formData: String,
    )

    private fun getJourney(journeyName: String): Journey<*> =
        journeys.find { it.journeyType.urlPathSegment.equals(journeyName, ignoreCase = true) }
            ?: throw IllegalArgumentException("Journey named \"$journeyName\" not found")

    private fun getStepId(
        journey: Journey<*>,
        stepName: String,
    ): StepId {
        // TODO I think this should return Step not StepId
        val stepIds = journey.steps.keys
        return stepIds.find { it.urlPathSegment.equals(stepName, ignoreCase = true) }
            ?: throw IllegalArgumentException("No step named \"$stepName\" found in journey \"${journey.journeyType.name}\"")
    }
}
