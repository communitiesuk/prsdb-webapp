package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.repository.NgdAddressRepository
import uk.gov.communities.prsdb.webapp.services.NgdAddressService.Companion.DATA_PACKAGE_VERSION_COMMENT_PREFIX
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class NgdAddressServiceTests {
    @Mock
    private lateinit var ngdAddressRepository: NgdAddressRepository

    @InjectMocks
    private lateinit var ngdAddressService: NgdAddressService

    @Test
    fun `getStoredDataPackageVersionId returns the ID stored in the comment`() {
        val dataPackageVersionId = "12345"
        whenever(ngdAddressRepository.findComment()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$dataPackageVersionId")
        assertEquals(dataPackageVersionId, ngdAddressService.getStoredDataPackageVersionId())
    }

    @Test
    fun `getStoredDataPackageVersionId returns null when comment is null`() {
        whenever(ngdAddressRepository.findComment()).thenReturn(null)
        assertNull(ngdAddressService.getStoredDataPackageVersionId())
    }

    @Test
    fun `getStoredDataPackageVersionId returns null when comment doesn't contain an ID`() {
        whenever(ngdAddressRepository.findComment()).thenReturn(DATA_PACKAGE_VERSION_COMMENT_PREFIX)
        assertNull(ngdAddressService.getStoredDataPackageVersionId())
    }

    @Test
    fun `setStoredDataPackageVersionId saves the ID in the comment`() {
        val dataPackageVersionId = "12345"
        ngdAddressService.setStoredDataPackageVersionId(dataPackageVersionId)

        val expectedComment = "$DATA_PACKAGE_VERSION_COMMENT_PREFIX$dataPackageVersionId"
        verify(ngdAddressRepository).saveComment(expectedComment)
    }
}
