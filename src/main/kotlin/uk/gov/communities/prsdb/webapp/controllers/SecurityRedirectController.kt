package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.SECURITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SECURITY_TXT_REDIRECT
import uk.gov.communities.prsdb.webapp.constants.WELL_KNOWN_PATH_SEGMENT

@PrsdbController
@RequestMapping
class SecurityRedirectController {
    @GetMapping("/$WELL_KNOWN_PATH_SEGMENT/$SECURITY_PATH_SEGMENT")
    fun redirectToSecurityTxt(response: HttpServletResponse) {
        response.sendRedirect(SECURITY_TXT_REDIRECT)
    }
}
