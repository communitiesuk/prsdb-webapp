package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.util.Locale

@Controller
@RequestMapping("/register-as-a-landlord")
class RegisterLandlordController(
    @Qualifier("messageSource")
    private val messageSource: MessageSource,
) {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute("title", messageSource.getMessage("registerAsALandlord.title", null, Locale("en")))
        return "registerAsALandlord"
    }
}
