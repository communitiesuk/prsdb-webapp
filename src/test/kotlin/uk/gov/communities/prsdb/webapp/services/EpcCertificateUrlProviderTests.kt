package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class EpcCertificateUrlProviderTests {
    @Test
    fun `getEpcCertificateUrl returns the correct URL for a valid certificate number`() {
        val epcCertificateUrlProvider = EpcCertificateUrlProvider("http://example.com/epc")

        val certificateNumber = "1234-5678-9012-3456-7890"

        val expectedUrl = "http://example.com/epc/1234-5678-9012-3456-7890"
        val actualUrl = epcCertificateUrlProvider.getEpcCertificateUrl(certificateNumber)
        assertEquals(expectedUrl, actualUrl)
    }
}
