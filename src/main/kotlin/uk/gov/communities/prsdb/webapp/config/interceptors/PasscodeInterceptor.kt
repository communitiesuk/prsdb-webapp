package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PASSCODE_REDIRECT_URL
import uk.gov.communities.prsdb.webapp.constants.SUBMITTED_PASSCODE
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController.Companion.INVALID_PASSCODE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController.Companion.PASSCODE_ENTRY_ROUTE
import uk.gov.communities.prsdb.webapp.helpers.URIQueryBuilder
import uk.gov.communities.prsdb.webapp.services.PasscodeService

class PasscodeInterceptor(
    private val passcodeService: PasscodeService,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        // Don't allow users who have claimed a passcode to access passcode pages
        val principal = request.userPrincipal
        if (principal != null && passcodeService.hasUserClaimedAPasscode(principal.name)) {
            return handleAuthenticatedUserWithClaimedPasscode(request, response)
        }

        // Only apply interceptor to non-passcode landlord routes
        val currentPath = request.requestURI
        if (!currentPath.startsWith("/$LANDLORD_PATH_SEGMENT/") || currentPath in passcodeRoutes) {
            return true
        }

        return if (principal == null) {
            handleUnauthenticatedUser(request, response)
        } else {
            handleAuthenticatedUserWithoutClaimedPasscode(request, response, principal.name)
        }
    }

    private fun handleAuthenticatedUserWithClaimedPasscode(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Boolean =
        if (request.requestURI in passcodeRoutes) {
            redirectToDashboardAndReturnFalse(response)
        } else {
            removePasscodeRedirectAndReturnTrue(request)
        }

    private fun handleUnauthenticatedUser(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Boolean =
        if (request.session.getAttribute(SUBMITTED_PASSCODE) != null) {
            removePasscodeRedirectAndReturnTrue(request)
        } else {
            redirectToPasscodeEntryAndReturnFalse(request, response)
        }

    private fun handleAuthenticatedUserWithoutClaimedPasscode(
        request: HttpServletRequest,
        response: HttpServletResponse,
        userId: String,
    ): Boolean {
        val submittedPasscode = request.session.getAttribute(SUBMITTED_PASSCODE) as String?

        return if (submittedPasscode != null) {
            handleAuthenticatedUserWithSessionPasscode(request, response, userId, submittedPasscode)
        } else {
            redirectToPasscodeEntryAndReturnFalse(request, response)
        }
    }

    private fun handleAuthenticatedUserWithSessionPasscode(
        request: HttpServletRequest,
        response: HttpServletResponse,
        userId: String,
        submittedPasscode: String,
    ): Boolean {
        val passcode = passcodeService.findPasscode(submittedPasscode) ?: return redirectToInvalidPasscodeAndReturnFalse(response)

        return if (passcode.baseUser == null) {
            handleUnclaimedPasscode(request, response, userId, submittedPasscode)
        } else {
            redirectToInvalidPasscodeAndReturnFalse(response)
        }
    }

    private fun handleUnclaimedPasscode(
        request: HttpServletRequest,
        response: HttpServletResponse,
        userId: String,
        submittedPasscode: String,
    ): Boolean =
        if (passcodeService.claimPasscodeForUser(submittedPasscode, userId)) {
            removePasscodeRedirectAndReturnTrue(request)
        } else {
            redirectToInvalidPasscodeAndReturnFalse(response)
        }

    private fun removePasscodeRedirectAndReturnTrue(request: HttpServletRequest): Boolean {
        request.session.removeAttribute(PASSCODE_REDIRECT_URL)
        return true
    }

    private fun redirectToDashboardAndReturnFalse(response: HttpServletResponse): Boolean {
        response.sendRedirect(LANDLORD_DASHBOARD_URL)
        return false
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

    private fun redirectToInvalidPasscodeAndReturnFalse(response: HttpServletResponse): Boolean {
        response.sendRedirect(INVALID_PASSCODE_ROUTE)
        return false
    }

    companion object {
        private val passcodeRoutes = listOf(PASSCODE_ENTRY_ROUTE, INVALID_PASSCODE_ROUTE)
    }
}
