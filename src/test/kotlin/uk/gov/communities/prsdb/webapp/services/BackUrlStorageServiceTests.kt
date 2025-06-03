package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_STORAGE_SESSION_ATTRIBUTE
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@ExtendWith(MockitoExtension::class)
class BackUrlStorageServiceTests {
    @Mock
    private lateinit var httpSession: HttpSession

    @Mock
    private lateinit var request: HttpServletRequest

    @InjectMocks
    private lateinit var backUrlStorageService: BackUrlStorageService

    private val requestUri = "/test"
    private val queryString = "param=value"
    private val exampleUrl = "$requestUri?$queryString"

    @BeforeEach
    fun setUp() {
        val servletRequestAttributes = ServletRequestAttributes(request)
        RequestContextHolder.setRequestAttributes(servletRequestAttributes)
    }

    @Nested
    inner class RememberCurrentUrlTests {
        @BeforeEach
        fun setUp() {
            whenever(request.requestURI).thenReturn(requestUri)
            whenever(request.queryString).thenReturn(queryString)
        }

        @Test
        fun `If available as a key, rememberCurrentUrl saves the current URL in the session by hashCode`() {
            val urlMapCaptor = argumentCaptor<Map<Int, String>>()

            val urlKey = backUrlStorageService.rememberCurrentUrlAndReturnId()
            verify(httpSession).setAttribute(
                eq(BACK_URL_STORAGE_SESSION_ATTRIBUTE),
                urlMapCaptor.capture(),
            )

            val url = urlMapCaptor.firstValue[urlKey]

            assertEquals(abs(url.hashCode()), urlKey)
        }

        @Test
        fun `If current URL is already saved by its hashCode, rememberCurrentUrl returns the current URLs key`() {
            whenever(httpSession.getAttribute(BACK_URL_STORAGE_SESSION_ATTRIBUTE))
                .thenReturn(mapOf(abs(exampleUrl.hashCode()) to exampleUrl))

            val urlKey = backUrlStorageService.rememberCurrentUrlAndReturnId()
            verify(httpSession, never()).setAttribute(anyOrNull(), anyOrNull())

            assertEquals(abs(exampleUrl.hashCode()), urlKey)
        }

        @Suppress("ktlint:standard:max-line-length")
        @Test
        fun `If another URL is already saved by the same hashCode, rememberCurrentUrl saves the current URL in the session with a different key`() {
            whenever(httpSession.getAttribute(BACK_URL_STORAGE_SESSION_ATTRIBUTE))
                .thenReturn(mapOf(abs(exampleUrl.hashCode()) to "another url"))

            val urlMapCaptor = argumentCaptor<Map<Int, String>>()

            val urlKey = backUrlStorageService.rememberCurrentUrlAndReturnId()
            verify(httpSession).setAttribute(
                eq(BACK_URL_STORAGE_SESSION_ATTRIBUTE),
                urlMapCaptor.capture(),
            )

            val url = urlMapCaptor.firstValue[urlKey]

            assertEquals(exampleUrl, url)
            assertNotEquals(urlKey, exampleUrl.hashCode())
        }

        @Suppress("ktlint:standard:max-line-length")
        @Test
        fun `If another URL is already saved by the same hashCode and the current URL has already been saved, rememberCurrentUrl returns the current URLs key`() {
            val usedKey = 12345
            whenever(httpSession.getAttribute(BACK_URL_STORAGE_SESSION_ATTRIBUTE))
                .thenReturn(mapOf(abs(exampleUrl.hashCode()) to "another url", usedKey to exampleUrl))

            val urlKey = backUrlStorageService.rememberCurrentUrlAndReturnId()
            verify(httpSession, never()).setAttribute(anyOrNull(), anyOrNull())

            assertEquals(usedKey, urlKey)
        }
    }

    @Test
    fun `getBackUrl returns the corresponding url in storage`() {
        val expectedUrl = "a url"
        val key = 277
        whenever(httpSession.getAttribute(BACK_URL_STORAGE_SESSION_ATTRIBUTE))
            .thenReturn(mapOf(key to expectedUrl))

        val url = backUrlStorageService.getBackUrl(key)

        assertEquals(expectedUrl, url)
    }
}
