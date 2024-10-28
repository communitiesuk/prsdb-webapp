package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

@Controller
@RequestMapping("/")
class ExampleStartPageController {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute(
            "landlordRegNum",
            RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, 205498766).toString(),
        )
        model.addAttribute("hideBetaBanner", true)
        return "demoStart"
    }
}
