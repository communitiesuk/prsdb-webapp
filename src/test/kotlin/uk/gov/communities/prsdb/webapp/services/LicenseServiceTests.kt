package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
import kotlin.reflect.full.hasAnnotation

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

    @Test
    fun `deleteLicence calls delete on the licenseRepository`() {
        val licence = License(LicensingType.HMO_MANDATORY_LICENCE, "LN123456")

        licenseService.deleteLicence(licence)

        verify(mockLicenseRepository).delete(licence)
    }

    @Test
    fun `updateLicence returns an updated licence when there is an existing licence and a new licence`() {
        val licence = License(LicensingType.HMO_MANDATORY_LICENCE, "LN123456")
        val newLicence = License(LicensingType.SELECTIVE_LICENCE, "SL123456")

        val updatedLicence = licenseService.updateLicence(licence, newLicence.licenseType, newLicence.licenseNumber)

        assertEquals(updatedLicence?.licenseType, newLicence.licenseType)
        assertEquals(updatedLicence?.licenseNumber, newLicence.licenseNumber)
    }

    @Test
    fun `updateLicence calls createLicence and returns the created licence when there is no existing licence and there is a new licence`() {
        val licenseType = LicensingType.SELECTIVE_LICENCE
        val licenceNumber = "SL123456"
        val expectedLicense = License(licenseType, licenceNumber)

        whenever(mockLicenseRepository.save(any(License::class.java))).thenReturn(expectedLicense)

        val updatedLicence = licenseService.updateLicence(null, licenseType, licenceNumber)

        verify(mockLicenseRepository).save(any(License::class.java))

        assertEquals(updatedLicence?.licenseType, expectedLicense.licenseType)
        assertEquals(updatedLicence?.licenseNumber, expectedLicense.licenseNumber)
    }

    @Test
    fun `updateLicence calls deleteLicence and returns null when there is an existing licence and the new licenceType is NO_LICENSING`() {
        val licence = License(LicensingType.HMO_MANDATORY_LICENCE, "LN123456")
        val newLicenceType = LicensingType.NO_LICENSING

        val updatedLicence = licenseService.updateLicence(licence, newLicenceType, null)

        verify(mockLicenseRepository).delete(licence)

        assertNull(updatedLicence)
    }

    @Test
    fun `updateLicence is annotated with @Transactional`() {
        assertTrue(licenseService::updateLicence.hasAnnotation<Transactional>())
    }
}
