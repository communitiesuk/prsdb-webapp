package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PASSCODE_REDIRECT_URL
import uk.gov.communities.prsdb.webapp.constants.SUBMITTED_PASSCODE
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController.Companion.PASSCODE_ALREADY_USED_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController.Companion.PASSCODE_ENTRY_ROUTE
import uk.gov.communities.prsdb.webapp.services.PasscodeService
import java.security.Principal

class PasscodeInterceptor(
    private val passcodeService: PasscodeService,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val currentPath = request.requestURI

        // Only apply interceptor to landlord pages
        if (!currentPath.startsWith("/$LANDLORD_PATH_SEGMENT/")) {
            return true
        }

        // Skip interceptor for passcode entry page and already used page to avoid redirect loops
        if (currentPath == PASSCODE_ENTRY_ROUTE || currentPath == PASSCODE_ALREADY_USED_ROUTE) {
            return true
        }

        val principal = request.userPrincipal
        val session = request.session
        val submittedPasscode = session.getAttribute(SUBMITTED_PASSCODE) as? String

        return if (principal == null) {
            // User is not logged in
            handleUnauthenticatedUser(request, response, submittedPasscode)
        } else {
            // User is logged in
            handleAuthenticatedUser(request, response, principal, submittedPasscode)
        }
    }

    private fun handleUnauthenticatedUser(
        request: HttpServletRequest,
        response: HttpServletResponse,
        submittedPasscode: String?,
    ): Boolean {
        return if (submittedPasscode != null) {
            // There is an entered passcode in the session, let the request proceed
            true
        } else {
            // No passcode in session, redirect to passcode entry page
            redirectToPasscodeEntry(request, response)
            false
        }
    }

    private fun handleAuthenticatedUser(
        request: HttpServletRequest,
        response: HttpServletResponse,
        principal: Principal,
        submittedPasscode: String?,
    ): Boolean {
        val userId = principal.name

        return if (submittedPasscode == null) {
            handleUserWithoutSessionPasscode(request, response, userId)
        } else {
            handleUserWithSessionPasscode(request, response, userId, submittedPasscode)
        }
    }

    private fun handleUserWithoutSessionPasscode(
        request: HttpServletRequest,
        response: HttpServletResponse,
        userId: String,
    ): Boolean {
        return if (passcodeService.hasUserClaimedPasscode(userId)) {
            // User has claimed a passcode, let them proceed
            true
        } else {
            // User hasn't claimed a passcode, redirect to passcode page
            redirectToPasscodeEntry(request, response)
            false
        }
    }

    private fun handleUserWithSessionPasscode(
        request: HttpServletRequest,
        response: HttpServletResponse,
        userId: String,
        submittedPasscode: String,
    ): Boolean {
        val passcode =
            passcodeService.findPasscode(submittedPasscode)
                ?: return redirectToPasscodeEntryAndReturnFalse(request, response)

        return when {
            passcode.baseUser == null -> handleUnclaimedPasscode(request, response, userId, submittedPasscode)
            passcodeService.isPasscodeClaimedByUser(submittedPasscode, userId) -> true
            else -> redirectToPasscodeAlreadyUsedAndReturnFalse(response)
        }
    }

    private fun handleUnclaimedPasscode(
        request: HttpServletRequest,
        response: HttpServletResponse,
        userId: String,
        submittedPasscode: String,
    ): Boolean {
        val claimed = passcodeService.claimPasscodeForUser(submittedPasscode, userId)
        return if (claimed) {
            true
        } else {
            redirectToPasscodeEntryAndReturnFalse(request, response)
        }
    }

    private fun redirectToPasscodeEntry(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        // Store the current URL as redirect target
        val currentUrl = request.requestURI + if (request.queryString != null) "?${request.queryString}" else ""
        request.session.setAttribute(PASSCODE_REDIRECT_URL, currentUrl)

        response.sendRedirect(PASSCODE_ENTRY_ROUTE)
    }

    private fun redirectToPasscodeAlreadyUsed(response: HttpServletResponse) {
        response.sendRedirect(PASSCODE_ALREADY_USED_ROUTE)
    }

    private fun redirectToPasscodeEntryAndReturnFalse(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Boolean {
        redirectToPasscodeEntry(request, response)
        return false
    }

    private fun redirectToPasscodeAlreadyUsedAndReturnFalse(response: HttpServletResponse): Boolean {
        redirectToPasscodeAlreadyUsed(response)
        return false
    }
}
