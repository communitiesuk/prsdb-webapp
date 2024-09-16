package uk.gov.communities.prsd.webapp.controllers

import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsd.webapp.constants.SERVICE_NAME

@Controller
class CustomErrorController : ErrorController {
    @RequestMapping("/error")
    fun handleError(
        request: HttpServletRequest,
        model: Model,
    ): String {
        model.addAttribute("serviceName", SERVICE_NAME)

        val status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)

        if (status != null) {
            val statusCode = status.toString().toInt()

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("contentHeader", "Page not found")
                model.addAttribute("title", "Page not found - $SERVICE_NAME - GOV.UK")
                return "error/404"
            }
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("contentHeader", "Access denied")
                model.addAttribute("title", "Access denied - $SERVICE_NAME - GOV.UK")
                return "error/403"
            }
        }
        model.addAttribute(
            "contentHeader",
            "Sorry, there is a problem with the service",
        )
        model.addAttribute(
            "title",
            "Sorry, there is a problem with the service - $SERVICE_NAME - GOV.UK",
        )
        return "error/500"
    }
}
