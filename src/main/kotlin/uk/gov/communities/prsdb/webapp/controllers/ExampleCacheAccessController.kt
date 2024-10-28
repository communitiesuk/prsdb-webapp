package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import uk.gov.communities.prsdb.webapp.services.ExampleCountingService

@Controller
class ExampleCacheAccessController(
    var counter: ExampleCountingService,
) {
    @GetMapping("/visit-count")
    fun visitCount(model: Model): String {
        val count = counter.getCountAndIncrement()
        model.addAttribute("title", "visitCount.title")
        model.addAttribute("contentHeader", "visitCount.header")
        model.addAttribute("contentHeaderParams", count.toString())
        return "index"
    }
}
