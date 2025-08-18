package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.PASSCODE_REDIRECT_URL
import uk.gov.communities.prsdb.webapp.constants.SUBMITTED_PASSCODE
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController.Companion.PASSCODE_ALREADY_USED_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController.Companion.PASSCODE_ENTRY_ROUTE
import uk.gov.communities.prsdb.webapp.services.PasscodeService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.security.Principal
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasscodeInterceptorTests {
    private lateinit var passcodeService: PasscodeService
    private lateinit var passcodeInterceptor: PasscodeInterceptor
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var session: HttpSession
    private lateinit var principal: Principal
    private val handler = Any()

    @BeforeEach
    fun setup() {
        passcodeService = mock(PasscodeService::class.java)
        passcodeInterceptor = PasscodeInterceptor(passcodeService)
        request = mock(HttpServletRequest::class.java)
        response = mock(HttpServletResponse::class.java)
        session = mock(HttpSession::class.java)
        principal = mock(Principal::class.java)

        whenever(request.session).thenReturn(session)
    }

    @Test
    fun `preHandle allows non-landlord paths`() {
        whenever(request.requestURI).thenReturn(LOCAL_AUTHORITY_DASHBOARD_URL)

        val result = passcodeInterceptor.preHandle(request, response, handler)

        assertTrue(result)
        verify(response, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle allows passcode entry route`() {
        whenever(request.requestURI).thenReturn(PASSCODE_ENTRY_ROUTE)

        val result = passcodeInterceptor.preHandle(request, response, handler)

        assertTrue(result)
        verify(response, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle allows passcode already used route`() {
        whenever(request.requestURI).thenReturn(PASSCODE_ALREADY_USED_ROUTE)

        val result = passcodeInterceptor.preHandle(request, response, handler)

        assertTrue(result)
        verify(response, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle redirects unauthenticated user without passcode to entry page`() {
        whenever(request.requestURI).thenReturn(LANDLORD_DASHBOARD_URL)
        whenever(request.queryString).thenReturn("param=value")
        whenever(request.userPrincipal).thenReturn(null)
        whenever(session.getAttribute(SUBMITTED_PASSCODE)).thenReturn(null)

        val result = passcodeInterceptor.preHandle(request, response, handler)

        assertFalse(result)
        verify(session).setAttribute(PASSCODE_REDIRECT_URL, "$LANDLORD_DASHBOARD_URL?param=value")
        verify(response).sendRedirect(PASSCODE_ENTRY_ROUTE)
    }

    @Test
    fun `preHandle allows unauthenticated user with valid passcode in session`() {
        whenever(request.requestURI).thenReturn(LANDLORD_DASHBOARD_URL)
        whenever(request.userPrincipal).thenReturn(null)
        whenever(session.getAttribute(SUBMITTED_PASSCODE)).thenReturn("ABCDEF")

        val result = passcodeInterceptor.preHandle(request, response, handler)

        assertTrue(result)
        verify(response, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle allows authenticated user who has claimed a passcode`() {
        whenever(request.requestURI).thenReturn(LANDLORD_DASHBOARD_URL)
        whenever(request.userPrincipal).thenReturn(principal)
        whenever(principal.name).thenReturn("userID")
        whenever(session.getAttribute(SUBMITTED_PASSCODE)).thenReturn(null)
        whenever(passcodeService.hasUserClaimedPasscode("userID")).thenReturn(true)

        val result = passcodeInterceptor.preHandle(request, response, handler)

        assertTrue(result)
        verify(response, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle redirects authenticated user without claimed passcode to entry page`() {
        whenever(request.requestURI).thenReturn(LANDLORD_DASHBOARD_URL)
        whenever(request.userPrincipal).thenReturn(principal)
        whenever(principal.name).thenReturn("userID")
        whenever(session.getAttribute(SUBMITTED_PASSCODE)).thenReturn(null)
        whenever(passcodeService.hasUserClaimedPasscode("userID")).thenReturn(false)

        val result = passcodeInterceptor.preHandle(request, response, handler)

        assertFalse(result)
        verify(response).sendRedirect(PASSCODE_ENTRY_ROUTE)
    }

    @Test
    fun `preHandle allows authenticated user with valid passcode claimed by same user`() {
        whenever(request.requestURI).thenReturn(LANDLORD_DASHBOARD_URL)
        whenever(request.userPrincipal).thenReturn(principal)
        whenever(principal.name).thenReturn("userID")
        whenever(session.getAttribute(SUBMITTED_PASSCODE)).thenReturn("ABCDEF")
        val passcode = MockLandlordData.createPasscode(code = "ABCDEF", baseUser = MockLandlordData.createOneLoginUser("userID"))
        whenever(passcodeService.findPasscode("ABCDEF")).thenReturn(passcode)
        whenever(passcodeService.isPasscodeClaimedByUser("ABCDEF", "userID")).thenReturn(true)

        val result = passcodeInterceptor.preHandle(request, response, handler)

        assertTrue(result)
        verify(response, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle redirects to already used page when passcode claimed by different user`() {
        whenever(request.requestURI).thenReturn(LANDLORD_DASHBOARD_URL)
        whenever(request.userPrincipal).thenReturn(principal)
        whenever(principal.name).thenReturn("userID")
        whenever(session.getAttribute(SUBMITTED_PASSCODE)).thenReturn("ABCDEF")
        val passcode = MockLandlordData.createPasscode(code = "ABCDEF", baseUser = MockLandlordData.createOneLoginUser("otherUserID"))
        whenever(passcodeService.findPasscode("ABCDEF")).thenReturn(passcode)
        whenever(passcodeService.isPasscodeClaimedByUser("ABCDEF", "userID")).thenReturn(false)

        val result = passcodeInterceptor.preHandle(request, response, handler)

        assertFalse(result)
        verify(response).sendRedirect(PASSCODE_ALREADY_USED_ROUTE)
    }

    @Test
    fun `preHandle claims unclaimed passcode for authenticated user`() {
        whenever(request.requestURI).thenReturn(LANDLORD_DASHBOARD_URL)
        whenever(request.userPrincipal).thenReturn(principal)
        whenever(principal.name).thenReturn("userID")
        whenever(session.getAttribute(SUBMITTED_PASSCODE)).thenReturn("ABCDEF")
        val passcode = MockLandlordData.createPasscode(code = "ABCDEF", baseUser = null)
        whenever(passcodeService.findPasscode("ABCDEF")).thenReturn(passcode)
        whenever(passcodeService.claimPasscodeForUser("ABCDEF", "userID")).thenReturn(true)

        val result = passcodeInterceptor.preHandle(request, response, handler)

        assertTrue(result)
        verify(passcodeService).claimPasscodeForUser("ABCDEF", "userID")
        verify(response, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle redirects to entry page if claiming an unclaimed passcode for authenticated user fails`() {
        whenever(request.requestURI).thenReturn(LANDLORD_DASHBOARD_URL)
        whenever(request.userPrincipal).thenReturn(principal)
        whenever(principal.name).thenReturn("userID")
        whenever(session.getAttribute(SUBMITTED_PASSCODE)).thenReturn("ABCDEF")
        val passcode = MockLandlordData.createPasscode(code = "ABCDEF", baseUser = null)
        whenever(passcodeService.findPasscode("ABCDEF")).thenReturn(passcode)
        whenever(passcodeService.claimPasscodeForUser("ABCDEF", "userID")).thenReturn(false)

        val result = passcodeInterceptor.preHandle(request, response, handler)

        assertFalse(result)
        verify(passcodeService).claimPasscodeForUser("ABCDEF", "userID")
        verify(response).sendRedirect(PASSCODE_ENTRY_ROUTE)
    }

    @Test
    fun `preHandle redirects to entry page when passcode not found`() {
        whenever(request.requestURI).thenReturn(LANDLORD_DASHBOARD_URL)
        whenever(request.userPrincipal).thenReturn(principal)
        whenever(principal.name).thenReturn("userID")
        whenever(session.getAttribute(SUBMITTED_PASSCODE)).thenReturn("INVALID")
        whenever(passcodeService.findPasscode("INVALID")).thenReturn(null)

        val result = passcodeInterceptor.preHandle(request, response, handler)

        assertFalse(result)
        verify(response).sendRedirect(PASSCODE_ENTRY_ROUTE)
    }
}
