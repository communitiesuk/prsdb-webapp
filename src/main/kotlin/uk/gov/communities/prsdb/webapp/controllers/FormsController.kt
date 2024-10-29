package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.services.JourneyService
import java.security.Principal

@Controller
@RequestMapping("/forms")
class FormsController(
    var session: HttpSession,
    private val journeyService: JourneyService,
) {
    @GetMapping("/{journeyName}/{stepName}")
    fun getForm(
        @PathVariable("journeyName") journeyName: String,
        @PathVariable("stepName") stepName: String,
        model: Model,
        session: HttpSession,
    ): ModelAndView {
        val context = session.getAttribute("FORM_CONTEXT") as? Map<String, Any> ?: emptyMap()
        return journeyService.getJourneyView(
            journeyName,
            stepName,
            context,
        )
    }

    @PostMapping("/{journeyName}/{stepName}", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun postForms(
        @PathVariable("journeyName") journeyName: String,
        @PathVariable("stepName") stepName: String,
        body: PostSubmission,
        principal: Principal,
    ): String {
        val context = session.getAttribute("FORM_CONTEXT") as? Map<String, Any> ?: emptyMap()
        val formContextId: Long? = session.getAttribute("FORM_CONTEXT_ID")?.toString()?.toLongOrNull()
        val updatedContext: Map<String, Any> =
            journeyService.updateFormContextAndGetNextStep(
                journeyName,
                stepName,
                principal.name,
                body.formData,
                formContextId,
                context,
            )

        session.setAttribute("FORM_CONTEXT", Json.encodeToString(updatedContext))
        session.setAttribute("FORM_CONTEXT_ID", formContextId)

        return journeyService.getRedirectUrl(
            journeyName,
            stepName,
            updatedContext,
            formContextId,
        )
    }

    class PostSubmission(
        val formData: Map<String, String>,
    )
}
