package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.services.ActiveInactiveService
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class InactiveLandlordInterceptorTests {
    private val mockRequest = MockHttpServletRequest()

    @Mock
    private lateinit var mockResponse: HttpServletResponse

    @Mock
    private lateinit var mockActiveInactiveService: ActiveInactiveService

    @InjectMocks
    private lateinit var interceptor: InactiveLandlordInterceptor

    @Test
    fun `preHandle allows the request when the current landlord is not inactive`() {
        whenever(mockActiveInactiveService.isCurrentLandlordInactive()).thenReturn(false)

        assertTrue(interceptor.preHandle(mockRequest, mockResponse, handler = Any()))
    }

    @Test
    fun `preHandle throws a 403 when the current landlord is inactive`() {
        whenever(mockActiveInactiveService.isCurrentLandlordInactive()).thenReturn(true)

        val exception =
            assertThrows<ResponseStatusException> {
                interceptor.preHandle(mockRequest, mockResponse, handler = Any())
            }
        assertEquals(HttpStatus.FORBIDDEN, exception.statusCode)
    }
}
