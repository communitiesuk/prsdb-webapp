package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class RegisteredAddressCacheTests {
    @Mock
    private lateinit var mockHttpSession: HttpSession

    private lateinit var registeredAddressCache: RegisteredAddressCache

    @BeforeEach
    fun setup() {
        registeredAddressCache = RegisteredAddressCache(mockHttpSession)
    }

    @Test
    fun `getCachedAddressRegisteredResult returns null if no results are cached`() {
        val uprn = 1234.toLong()
        whenever(mockHttpSession.getAttribute("addressRegisteredResults")).thenReturn(null)
        assertNull(registeredAddressCache.getCachedAddressRegisteredResult(uprn))
    }

    @Test
    fun `getCachedAddressRegisteredResult returns null if no matching result is cached`() {
        val uprn = 1234.toLong()
        whenever(mockHttpSession.getAttribute("addressRegisteredResults")).thenReturn(mapOf(5678.toString() to true))
        assertNull(registeredAddressCache.getCachedAddressRegisteredResult(uprn))
    }

    @Test
    fun `getCachedAddressRegisteredResult returns true if the cached result is true`() {
        val uprn = 1234.toLong()
        whenever(mockHttpSession.getAttribute("addressRegisteredResults")).thenReturn(mapOf(uprn.toString() to true))
        assertTrue(registeredAddressCache.getCachedAddressRegisteredResult(uprn) ?: false)
    }

    @Test
    fun `getCachedAddressRegisteredResult returns false if the cached result is false`() {
        val uprn = 1234.toLong()
        whenever(mockHttpSession.getAttribute("addressRegisteredResults")).thenReturn(mapOf(uprn.toString() to false))
        assertFalse(registeredAddressCache.getCachedAddressRegisteredResult(uprn) ?: true)
    }

    @Test
    fun `setCachedAddressRegisteredResult adds the result keyed by the uprn to the existing results cache`() {
        whenever(mockHttpSession.getAttribute("addressRegisteredResults")).thenReturn(mapOf(5678.toString() to true))

        val uprn = 1234.toLong()

        val expectedNewCache = mapOf(5678.toString() to true, uprn.toString() to false)

        registeredAddressCache.setCachedAddressRegisteredResult(uprn, false)
        val addressRegisteredResultCaptor = argumentCaptor<MutableMap<String, Boolean>>()
        verify(mockHttpSession).setAttribute(eq("addressRegisteredResults"), addressRegisteredResultCaptor.capture())
        Assertions.assertEquals(expectedNewCache, addressRegisteredResultCaptor.firstValue)
    }
}
