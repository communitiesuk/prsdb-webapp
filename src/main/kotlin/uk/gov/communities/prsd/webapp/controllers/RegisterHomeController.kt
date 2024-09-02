package uk.gov.communities.prsd.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/registration")
class RegisterHomeController {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute("contentHeader", "Register a home to rent")
        model.addAttribute("title", "Register a home to rent")
        model.addAttribute("serviceName", "Private Rented Sector Database")
        return "index"
    }
}
