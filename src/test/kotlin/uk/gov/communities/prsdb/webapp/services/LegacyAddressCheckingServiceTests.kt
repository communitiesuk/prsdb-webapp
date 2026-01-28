package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository

@ExtendWith(MockitoExtension::class)
class LegacyAddressCheckingServiceTests {
    @Mock
    private lateinit var mockPropertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var mockRegisteredAddressCache: RegisteredAddressCache

    @InjectMocks
    private lateinit var legacyAddressCheckingService: LegacyAddressCheckingService

    @ParameterizedTest
    @CsvSource("true", "false")
    fun `getIsAddressRegistered returns the expected value when the given uprn is cached`(expectedValue: Boolean) {
        // Arrange
        val uprn = 0L
        whenever(mockRegisteredAddressCache.getCachedAddressRegisteredResult(uprn)).thenReturn(expectedValue)

        // Act
        val result = legacyAddressCheckingService.getIsAddressRegistered(uprn)

        // Assert
        assertEquals(expectedValue, result)
    }

    @ParameterizedTest
    @CsvSource("true", "false")
    fun `getAddressIsRegistered caches and returns the expected value when the given uprn is not cached`(expectedValue: Boolean) {
        // Arrange
        val uprn = 0L
        whenever(mockRegisteredAddressCache.getCachedAddressRegisteredResult(uprn)).thenReturn(null)
        whenever(mockPropertyOwnershipRepository.existsByIsActiveTrueAndAddress_Uprn(uprn)).thenReturn(expectedValue)

        // Act
        val result = legacyAddressCheckingService.getIsAddressRegistered(uprn)

        // Assert
        verify(mockRegisteredAddressCache).setCachedAddressRegisteredResult(uprn, result)
        assertEquals(expectedValue, result)
    }

    @Test
    fun `getAddressIsRegistered ignores the cache when ignoreCache is true`() {
        // Arrange
        val uprn = 0L
        val expectedValue = true
        whenever(mockPropertyOwnershipRepository.existsByIsActiveTrueAndAddress_Uprn(uprn)).thenReturn(expectedValue)

        // Act
        val result = legacyAddressCheckingService.getIsAddressRegistered(uprn, ignoreCache = true)

        // Assert
        verify(mockRegisteredAddressCache, never()).getCachedAddressRegisteredResult(uprn)
        assertEquals(expectedValue, result)
    }
}
