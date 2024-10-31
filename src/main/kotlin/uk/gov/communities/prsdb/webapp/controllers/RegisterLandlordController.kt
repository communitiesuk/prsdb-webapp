package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.multipageforms.JourneyData

@Controller
@RequestMapping("/register-as-a-landlord")
class RegisterLandlordController {
    @GetMapping
    fun index(model: Model): String = "registerAsALandlord"

    @GetMapping("quick-break")
    fun quickBreak(): String = "quickBreak"

    @GetMapping("check-answers")
    fun checkAnswers(
        model: Model,
        session: HttpSession,
    ): String {
        val journeyData = session.getAttribute("journeyData") as? JourneyData ?: mutableMapOf()
        model.addAttribute("journeyData", journeyData)
        return "checkAnswersLandlord"
    }

    @GetMapping("check-phone-numbers")
    fun checkPhoneNumbers(
        model: Model,
        session: HttpSession,
    ): String {
        val journeyData = session.getAttribute("journeyData") as? JourneyData ?: mutableMapOf()
        val savedForms = journeyData["phone-number"] ?: emptyList()
        model.addAttribute("phoneNumbers", savedForms)
        return "checkPhoneNumbersLandlord"
    }
}
