package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.client.RestClient
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME

@Controller
@RequestMapping("/example")
class ExampleThirdPartyAPIController {
    // Note: Purpose is to demo stubbing of 3rd party APIs locally, NOT to demo how to call the APIs
    @GetMapping
    fun index(model: Model): String {
        val restClient = RestClient.create()
        val headerContent =
            restClient
                .get()
                .uri("http://localhost:8080/example-api")
                .retrieve()
                .body(String::class.java)
        model.addAttribute("contentHeader", headerContent)
        model.addAttribute("title", "Third party API demo")
        model.addAttribute("serviceName", SERVICE_NAME)
        return "index"
    }
}
