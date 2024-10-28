package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.config.JourneyConfig
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyStep
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.services.JourneyService
import java.security.Principal

// TODO add types to both path parameters
@Controller
@RequestMapping("/forms")
class FormsController(
    var session: HttpSession,
    private val journeyService: JourneyService,
    private val journeyConfig: JourneyConfig,
) {
    @GetMapping("/{journeyType}/{journeyStep}")
    fun getForm(
        @PathVariable("journeyType") journeyType: String,
        @PathVariable("journeyStep") journeyStep: String,
    ): String {
        // TODO dependency eject model in controller
        // TODO once Page has returned all attributes, loop over them to add to model attributes before returning view
        val context: String? = session.getAttribute("FORM_CONTEXT")?.toString()
        journeyService.getJourneyView(
            JourneyType.valueOf(journeyType),
            JourneyStep.valueOf(journeyStep),
            context?.let { journeyService.getMappedData(it) },
        )
        return "index"
    }

    @PostMapping("/{journeyType}/{journeyStep}")
    fun postForms(
        @PathVariable("journeyType") journeyType: String,
        @PathVariable("journeyStep") journeyStep: String,
        body: PostSubmission,
        principal: Principal,
    ): String {
        val context: String? = session.getAttribute("FORM_CONTEXT")?.toString()
        val formContextId: Long? = session.getAttribute("FORM_CONTEXT_ID")?.toString()?.toLongOrNull()
        val updatedContext: Map<String, JsonElement> =
            journeyService.updateFormContextAndGetNextStep(
                JourneyType.valueOf(journeyType),
                JourneyStep.valueOf(journeyStep),
                principal.name,
                journeyService.getMappedData(body.formData),
                formContextId,
                context?.let { journeyService.getMappedData(it) },
            )

        session.setAttribute("FORM_CONTEXT", Json.encodeToString(updatedContext))
        session.setAttribute("FORM_CONTEXT_ID", formContextId)

        return journeyService.getRedirectUrl(
            JourneyType.valueOf(journeyType),
            JourneyStep.valueOf(journeyStep),
            updatedContext,
            formContextId,
        )
    }

    class PostSubmission(
        val formData: String,
    )
}
