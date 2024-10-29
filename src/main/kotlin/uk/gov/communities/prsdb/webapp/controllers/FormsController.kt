package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.services.JourneyService
import java.security.Principal

@Controller
@RequestMapping("/forms")
class FormsController(
    var session: HttpSession,
    private val journeyService: JourneyService,
) {
    // TODO-PRSD-422 both endpoints should also take an optional query parameter (it should probably be an integer)
    // This will be required if the same journeyStep has to be repeated in a journey e.g. is there are two interested parties being registered we will need to differentiate between them
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
        @RequestParam formDataMap: Map<String, String>,
        principal: Principal,
    ): String {
        val context = session.getAttribute("FORM_CONTEXT") as? Map<String, Any> ?: emptyMap()
        // TODO-PRSD-422 Remove formContextId from session - it should be stored as part of the context and accessed from there
        val formContextId: Long? = session.getAttribute("FORM_CONTEXT_ID")?.toString()?.toLongOrNull()
        val updatedContext: Map<String, Any> =
            journeyService.updateFormContext(
                journeyName,
                stepName,
                principal.name,
                formDataMap,
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
}
