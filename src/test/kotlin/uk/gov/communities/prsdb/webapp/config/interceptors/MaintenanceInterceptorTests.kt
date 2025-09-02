package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockHttpServletRequest
import uk.gov.communities.prsdb.webapp.constants.ASSETS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.MaintenanceController
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class MaintenanceInterceptorTests {
    private val mockRequest = MockHttpServletRequest()

    @Mock
    private lateinit var mockResponse: HttpServletResponse

    @InjectMocks
    private lateinit var maintenanceInterceptor: MaintenanceInterceptor

    private fun callPreHandle() = maintenanceInterceptor.preHandle(mockRequest, mockResponse, handler = Any())

    @Test
    fun `preHandle allows requests to the maintenance endpoint`() {
        mockRequest.requestURI = MaintenanceController.MAINTENANCE_ROUTE
        assertTrue(callPreHandle())
        verify(mockResponse, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle allows requests to the assets endpoint`() {
        mockRequest.requestURI = "/$ASSETS_PATH_SEGMENT/someAsset.css"
        assertTrue(callPreHandle())
        verify(mockResponse, never()).sendRedirect(anyString())
    }

    // Check an endpoint with a /landlord prefix
    @Test
    fun `preHandle redirects landlord dashboard requests to the maintenance endpoint`() {
        mockRequest.requestURI = LANDLORD_DASHBOARD_URL
        assertFalse(callPreHandle())
        verify(mockResponse).sendRedirect(MaintenanceController.MAINTENANCE_ROUTE)
    }

    // Check an endpoint with a /local-council prefix
    @Test
    fun `preHandle redirects local council dashboard requests to the maintenance endpoint`() {
        mockRequest.requestURI = LOCAL_AUTHORITY_DASHBOARD_URL
        assertFalse(callPreHandle())
        verify(mockResponse).sendRedirect(MaintenanceController.MAINTENANCE_ROUTE)
    }

    // Check an endpoint with no prefix
    @Test
    fun `preHandle redirects signout requests to the maintenance endpoint`() {
        mockRequest.requestURI = "/signout"
        assertFalse(callPreHandle())
        verify(mockResponse).sendRedirect(MaintenanceController.MAINTENANCE_ROUTE)
    }
}
