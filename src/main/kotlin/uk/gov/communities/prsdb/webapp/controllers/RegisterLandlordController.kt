package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.services.MultiPageFormSessionService

@Controller
@RequestMapping("/register-as-a-landlord")
class RegisterLandlordController(
    private val sessionService: MultiPageFormSessionService,
) {
    @GetMapping
    fun index(model: Model): String = "registerAsALandlord"

    @GetMapping("quick-break")
    fun quickBreak(): String = "quickBreak"

    @GetMapping("check-answers")
    fun checkAnswers(model: Model): String {
        val journeyData = sessionService.getJourneyData()
        model.addAttribute("journeyData", journeyData)
        return "checkAnswersLandlord"
    }

    @GetMapping("check-phone-numbers")
    fun checkPhoneNumbers(model: Model): String {
        val journeyData = sessionService.getJourneyData()
        val savedForms = journeyData["phone-number"] ?: emptyList()
        model.addAttribute("phoneNumbers", savedForms)
        return "checkPhoneNumbersLandlord"
    }
}
