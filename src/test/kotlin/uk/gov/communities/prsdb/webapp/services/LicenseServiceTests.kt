package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.repository.LicenseRepository

@ExtendWith(MockitoExtension::class)
class LicenseServiceTests {
    @Mock
    private lateinit var mockLicenseRepository: LicenseRepository

    @InjectMocks
    private lateinit var licenseService: LicenseService

    @Test
    fun `createLicense creates a license`() {
        val licenseType = LicensingType.SELECTIVE_LICENCE
        val licenceNumber = "TestLicenseNumber"
        val expectedLicense = License(licenseType, licenceNumber)

        whenever(mockLicenseRepository.save(any(License::class.java))).thenReturn(expectedLicense)

        licenseService.createLicense(licenseType, licenceNumber)

        val licenseCaptor = captor<License>()
        verify(mockLicenseRepository).save(licenseCaptor.capture())
        assertTrue(ReflectionEquals(expectedLicense).matches(licenseCaptor.value))
    }
}
