package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
) {
    @GetMapping("/{journeyType}/{journeyStep}")
    fun getForm(
        @PathVariable("journeyType") journeyType: String,
        @PathVariable("journeyStep") journeyStep: String,
        @RequestParam(required = false, name = "formContextId") formContextId: Long,
        principal: Principal,
    ): String {
        val context: String? = session.getAttribute("FORM_CONTEXT").toString()
        journeyService.getJourneyView(
            JourneyType.valueOf(journeyType),
            JourneyStep.valueOf(journeyStep),
            principal.name,
            formContextId,
            context,
        )
        return "index"
    }

    @PostMapping("/{journeyType}/{journeyStep}")
    fun postForms(
        @PathVariable("journeyType") journeyType: String,
        @PathVariable("journeyStep") journeyStep: String,
        @RequestParam("formData") formData: String,
        @RequestParam(required = false, name = "formContextId") formContextId: Long,
        principal: Principal,
    ): String {
        val context =
            journeyService.updateFormContextAndGetNextStep(
                JourneyType.valueOf(journeyType),
                JourneyStep.valueOf(journeyStep),
                principal.name,
                formData,
                formContextId,
            )
        session.setAttribute("FORM_CONTEXT", context)

        // return redirect uri, think this should be a speerate method
        return "index"
    }
}
