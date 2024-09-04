package uk.gov.communities.prsd.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import uk.gov.communities.prsd.webapp.constants.SERVICE_NAME
import uk.gov.communities.prsd.webapp.services.ExampleCountingService

@Controller
class ExampleCacheAccessController(
    var counter: ExampleCountingService,
) {
    @GetMapping("/visit-count")
    fun visitCount(model: Model): String {
        var count = counter.getCountAndIncrement()
        model.addAttribute("contentHeader", "You have visited $count times")
        model.addAttribute("title", "Visit count")
        model.addAttribute("serviceName", SERVICE_NAME)
        return "index"
    }
}
