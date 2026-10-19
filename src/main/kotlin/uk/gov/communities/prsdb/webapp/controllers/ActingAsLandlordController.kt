package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpSession
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.ACTING_USER_ID
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL

@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
@PrsdbController
@RequestMapping("/$SYSTEM_OPERATOR_PATH_SEGMENT")
class ActingAsLandlordController(
    private val environment: Environment,
) {
    @GetMapping("/$LOCAL_ACT_AS_PATH_SEGMENT")
    fun startActing(
        @RequestParam userId: String,
        session: HttpSession,
    ): String {
        if (!environment.activeProfiles.contains("local")) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
        resetSession(session)
        session.setAttribute(ACTING_USER_ID, userId)
        return "redirect:/$SYSTEM_OPERATOR_PATH_SEGMENT$LANDLORD_DASHBOARD_URL"
    }

    @PostMapping("/$STOP_ACTING_PATH_SEGMENT")
    fun stopActing(session: HttpSession): String {
        resetSession(session)
        return "redirect:$SYSTEM_OPERATOR_DASHBOARD_URL"
    }

    private fun resetSession(session: HttpSession) {
        session.attributeNames
            .toList()
            .filterNot { it == SPRING_SECURITY_CONTEXT_KEY }
            .forEach { session.removeAttribute(it) }
    }

    companion object {
        const val LOCAL_ACT_AS_PATH_SEGMENT = "local-act-as"
        const val STOP_ACTING_PATH_SEGMENT = "stop-acting"
    }
}
