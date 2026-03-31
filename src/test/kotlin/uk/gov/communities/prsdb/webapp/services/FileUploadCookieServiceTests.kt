package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService.Companion.FILE_UPLOAD_COOKIE_NAME
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class FileUploadCookieServiceTests {
    @Mock
    private lateinit var mockTokenCookieService: TokenCookieService

    @Mock
    private lateinit var mockRequest: HttpServletRequest

    @Mock
    private lateinit var mockResponse: HttpServletResponse

    @InjectMocks
    private lateinit var fileUploadCookieService: FileUploadCookieService

    private val requestUri = "/some/path"

    @Nested
    inner class AddFileUploadCookieToResponse {
        @Test
        fun `addFileUploadCookieToResponse creates a cookie and adds it to the response`() {
            val cookie = Cookie(FILE_UPLOAD_COOKIE_NAME, "token")
            whenever(mockRequest.requestURI).thenReturn(requestUri)
            whenever(mockTokenCookieService.createCookieForValue(FILE_UPLOAD_COOKIE_NAME, requestUri)).thenReturn(cookie)

            fileUploadCookieService.addFileUploadCookieToResponse()

            verify(mockTokenCookieService).createCookieForValue(FILE_UPLOAD_COOKIE_NAME, requestUri)
            verify(mockResponse).addCookie(cookie)
        }
    }

    @Nested
    inner class ValidateAndUseToken {
        private val token = "valid-token"

        @BeforeEach
        fun setUp() {
            whenever(mockRequest.requestURI).thenReturn(requestUri)
        }

        @Test
        fun `validateAndUseToken uses the token when it is valid`() {
            whenever(mockTokenCookieService.isTokenForCookieValue(token, requestUri)).thenReturn(true)

            fileUploadCookieService.validateAndUseToken(token)

            verify(mockTokenCookieService).useToken(token)
        }

        @Test
        fun `validateAndUseToken throws ResponseStatusException when the token is invalid`() {
            whenever(mockTokenCookieService.isTokenForCookieValue(token, requestUri)).thenReturn(false)

            val exception =
                assertThrows<ResponseStatusException> {
                    fileUploadCookieService.validateAndUseToken(token)
                }

            assertEquals(400, exception.statusCode.value())
        }
    }
}
