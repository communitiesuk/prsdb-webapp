package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyStep
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.services.JourneyService
import java.security.Principal

// TODO add types to both path parameters
@Controller
@RequestMapping("/forms")
class FormsController(
    private val journeyService: JourneyService,
) {
    @GetMapping("/{journeyType}/{journeyStep}")
    fun getForm(
        @PathVariable("journeyType") journeyType: String,
        @PathVariable("journeyStep") journeyStep: String,
        model: Model,
    ): String {
        model.addAttribute("contentHeader", "getForms for $journeyType with $journeyStep")
        model.addAttribute("title", "getForms for $journeyType with $journeyStep")
        model.addAttribute("serviceName", SERVICE_NAME)
        return "index"
    }

    @PostMapping("/{journeyType}/{journeyStep}")
    fun postForms(
        @PathVariable("journeyType") journeyType: String,
        @PathVariable("journeyStep") journeyStep: String,
        @RequestParam("formData") formData: String,
        principal: Principal,
    ): String {
        journeyService.updateFormContextAndGetNextStep(
            JourneyType.valueOf(journeyType),
            JourneyStep.valueOf(journeyStep),
            principal.name,
            formData,
        )

        return "index"
    }
}
