package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpServletRequest
import uk.gov.communities.prsdb.webapp.constants.PASSCODE_REDIRECT_URL
import uk.gov.communities.prsdb.webapp.constants.SUBMITTED_PASSCODE
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController.Companion.PASSCODE_ALREADY_USED_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController.Companion.PASSCODE_ENTRY_ROUTE
import uk.gov.communities.prsdb.webapp.services.PasscodeService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class PasscodeInterceptorTests {
    private val userId = "userId"

    private val mockRequest = MockHttpServletRequest()

    @Mock
    private lateinit var mockSession: HttpSession

    @Mock
    private lateinit var mockResponse: HttpServletResponse

    @Mock
    private lateinit var mockPasscodeService: PasscodeService

    @InjectMocks
    private lateinit var passcodeInterceptor: PasscodeInterceptor

    @BeforeEach
    fun setup() {
        mockRequest.setSession(mockSession)
    }

    private fun callPreHandle() = passcodeInterceptor.preHandle(mockRequest, mockResponse, handler = Any())

    @Test
    fun `preHandle allows authenticated users with a claimed passcode who aren't trying to access passcode routes`() {
        mockRequest.requestURI = LANDLORD_DASHBOARD_URL
        mockRequest.setUserPrincipal { userId }
        whenever(mockPasscodeService.hasUserClaimedAPasscode(userId)).thenReturn(true)

        assertTrue(callPreHandle())
        verify(mockResponse, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle redirects authenticated users with a claimed passcode who are trying to access passcode routes`() {
        mockRequest.requestURI = PASSCODE_ENTRY_ROUTE
        mockRequest.setUserPrincipal { userId }
        whenever(mockPasscodeService.hasUserClaimedAPasscode(userId)).thenReturn(true)

        assertFalse(callPreHandle())
        verify(mockResponse).sendRedirect(LANDLORD_DASHBOARD_URL)
    }

    @Test
    fun `preHandle allows non-landlord paths`() {
        mockRequest.requestURI = LOCAL_AUTHORITY_DASHBOARD_URL

        assertTrue(callPreHandle())
        verify(mockResponse, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle allows passcode entry route when user has not claimed a passcode`() {
        mockRequest.requestURI = PASSCODE_ENTRY_ROUTE

        assertTrue(callPreHandle())
        verify(mockResponse, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle allows passcode already used route when user has not claimed a passcode`() {
        mockRequest.requestURI = PASSCODE_ENTRY_ROUTE

        assertTrue(callPreHandle())
        verify(mockResponse, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle redirects unauthenticated users without a session passcode to entry page`() {
        mockRequest.requestURI = LANDLORD_DASHBOARD_URL
        mockRequest.queryString = "param=value"
        mockRequest.userPrincipal = null
        whenever(mockSession.getAttribute(SUBMITTED_PASSCODE)).thenReturn(null)

        assertFalse(callPreHandle())
        verify(mockSession).setAttribute(PASSCODE_REDIRECT_URL, "$LANDLORD_DASHBOARD_URL?param=value")
        verify(mockResponse).sendRedirect(PASSCODE_ENTRY_ROUTE)
    }

    @Test
    fun `preHandle allows unauthenticated users with a session passcode`() {
        mockRequest.requestURI = LANDLORD_DASHBOARD_URL
        mockRequest.userPrincipal = null
        whenever(mockSession.getAttribute(SUBMITTED_PASSCODE)).thenReturn("ABCDEF")

        assertTrue(callPreHandle())
        verify(mockResponse, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle redirects authenticated users without a claimed or session passcode to entry page`() {
        mockRequest.requestURI = LANDLORD_DASHBOARD_URL
        mockRequest.setUserPrincipal { userId }
        whenever(mockPasscodeService.hasUserClaimedAPasscode(userId)).thenReturn(false)
        whenever(mockSession.getAttribute(SUBMITTED_PASSCODE)).thenReturn(null)

        assertFalse(callPreHandle())
        verify(mockSession).setAttribute(PASSCODE_REDIRECT_URL, LANDLORD_DASHBOARD_URL)
        verify(mockResponse).sendRedirect(PASSCODE_ENTRY_ROUTE)
    }

    @Test
    fun `preHandle redirects authenticated users without a claimed or valid session passcode to entry page`() {
        mockRequest.requestURI = LANDLORD_DASHBOARD_URL
        mockRequest.setUserPrincipal { userId }
        whenever(mockPasscodeService.hasUserClaimedAPasscode(userId)).thenReturn(false)
        val sessionPasscode = "INVALID"
        whenever(mockSession.getAttribute(SUBMITTED_PASSCODE)).thenReturn(sessionPasscode)
        whenever(mockPasscodeService.findPasscode(sessionPasscode)).thenReturn(null)

        assertFalse(callPreHandle())
        verify(mockSession).setAttribute(PASSCODE_REDIRECT_URL, LANDLORD_DASHBOARD_URL)
        verify(mockResponse).sendRedirect(PASSCODE_ENTRY_ROUTE)
    }

    @Test
    fun `preHandle allows authenticated users with a valid unclaimed session passcode to claim it`() {
        mockRequest.requestURI = LANDLORD_DASHBOARD_URL
        mockRequest.setUserPrincipal { userId }
        val passcode = MockLandlordData.createPasscode(code = "ABCDEF", baseUser = null)
        whenever(mockSession.getAttribute(SUBMITTED_PASSCODE)).thenReturn(passcode.passcode)
        whenever(mockPasscodeService.findPasscode(passcode.passcode)).thenReturn(passcode)
        whenever(mockPasscodeService.claimPasscodeForUser(passcode.passcode, userId)).thenReturn(true)

        assertTrue(callPreHandle())
        verify(mockPasscodeService).claimPasscodeForUser(passcode.passcode, userId)
        verify(mockResponse, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle redirects authenticated users with a valid unclaimed session passcode to entry page if claiming it fails`() {
        mockRequest.requestURI = LANDLORD_DASHBOARD_URL
        mockRequest.setUserPrincipal { userId }
        whenever(mockPasscodeService.hasUserClaimedAPasscode(userId)).thenReturn(false)
        val passcode = MockLandlordData.createPasscode(code = "ABCDEF", baseUser = null)
        whenever(mockSession.getAttribute(SUBMITTED_PASSCODE)).thenReturn(passcode.passcode)
        whenever(mockPasscodeService.findPasscode(passcode.passcode)).thenReturn(passcode)
        whenever(mockPasscodeService.claimPasscodeForUser(passcode.passcode, userId)).thenReturn(false)

        assertFalse(callPreHandle())
        verify(mockPasscodeService).claimPasscodeForUser(passcode.passcode, userId)
        verify(mockSession).setAttribute(PASSCODE_REDIRECT_URL, LANDLORD_DASHBOARD_URL)
        verify(mockResponse).sendRedirect(PASSCODE_ENTRY_ROUTE)
    }

    @Test
    fun `preHandle redirects authenticated users who try to claim already-used passcodes to already used page`() {
        mockRequest.requestURI = LANDLORD_DASHBOARD_URL
        mockRequest.setUserPrincipal { userId }
        whenever(mockPasscodeService.hasUserClaimedAPasscode(userId)).thenReturn(false)
        val passcode = MockLandlordData.createPasscode(code = "ABCDEF", baseUser = MockLandlordData.createOneLoginUser())
        whenever(mockSession.getAttribute(SUBMITTED_PASSCODE)).thenReturn(passcode.passcode)
        whenever(mockPasscodeService.findPasscode(passcode.passcode)).thenReturn(passcode)

        assertFalse(callPreHandle())
        verify(mockResponse).sendRedirect(PASSCODE_ALREADY_USED_ROUTE)
    }
}
