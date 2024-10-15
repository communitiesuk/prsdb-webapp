package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.services.RegistrationNumberService

@Controller
@RequestMapping("/")
class ExampleStartPageController(
    val regNumService: RegistrationNumberService,
) {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute("contentHeader", "Welcome to the Private Rental Sector Database")
        model.addAttribute("title", "Private Rental Sector Database")
        model.addAttribute("serviceName", SERVICE_NAME)
        model.addAttribute(
            "landlordRegNum",
            regNumService.formatRegNum(RegistrationNumber(RegistrationNumberType.LANDLORD, 205498766)),
        )
        model.addAttribute("startButtonHref", "/registration")
        model.addAttribute("startButtonText", "Start now")
        return "demoStart"
    }
}
