package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PASSCODE_REDIRECT_URL
import uk.gov.communities.prsdb.webapp.constants.SUBMITTED_PASSCODE
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController.Companion.PASSCODE_ALREADY_USED_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController.Companion.PASSCODE_ENTRY_ROUTE
import uk.gov.communities.prsdb.webapp.helpers.URIQueryBuilder
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

        // Only apply interceptor to non-passcode landlord routes
        val passcodeRoutes = listOf(PASSCODE_ENTRY_ROUTE, PASSCODE_ALREADY_USED_ROUTE)
        if (!currentPath.startsWith("/$LANDLORD_PATH_SEGMENT/") || currentPath in passcodeRoutes) {
            return true
        }

        val principal = request.userPrincipal
        val submittedPasscode = request.session.getAttribute(SUBMITTED_PASSCODE) as? String

        return if (principal == null) {
            handleUnauthenticatedUser(request, response, submittedPasscode)
        } else {
            handleAuthenticatedUser(request, response, principal, submittedPasscode)
        }
    }

    private fun handleUnauthenticatedUser(
        request: HttpServletRequest,
        response: HttpServletResponse,
        submittedPasscode: String?,
    ): Boolean =
        if (submittedPasscode != null) {
            true
        } else {
            redirectToPasscodeEntryAndReturnFalse(request, response)
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
    ): Boolean =
        if (passcodeService.hasUserClaimedPasscode(userId)) {
            true
        } else {
            redirectToPasscodeEntryAndReturnFalse(request, response)
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
    ): Boolean =
        if (passcodeService.claimPasscodeForUser(submittedPasscode, userId)) {
            true
        } else {
            redirectToPasscodeEntryAndReturnFalse(request, response)
        }

    private fun redirectToPasscodeEntryAndReturnFalse(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Boolean {
        val currentUrl = URIQueryBuilder.fromHTTPServletRequest(request).build().toUriString()
        request.session.setAttribute(PASSCODE_REDIRECT_URL, currentUrl)

        response.sendRedirect(PASSCODE_ENTRY_ROUTE)
        return false
    }

    private fun redirectToPasscodeAlreadyUsedAndReturnFalse(response: HttpServletResponse): Boolean {
        response.sendRedirect(PASSCODE_ALREADY_USED_ROUTE)
        return false
    }
}
