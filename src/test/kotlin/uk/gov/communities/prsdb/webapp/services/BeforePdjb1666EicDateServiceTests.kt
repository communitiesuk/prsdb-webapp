package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import java.time.LocalDate

class BeforePdjb1666EicDateServiceTests {
    private val service = BeforePdjb1666EicDateService()

    @Test
    fun `issueDateFrom returns five years before the stored expiry date`() {
        val propertyCompliance =
            PropertyComplianceBuilder()
                .withElectricalSafety(expiryDate = LocalDate.of(2030, 6, 1), certType = CertificateType.Eic)
                .build()

        assertEquals(LocalDate.of(2025, 6, 1), service.issueDateFrom(propertyCompliance))
    }

    @Test
    fun `expiryDateFrom returns the stored expiry date`() {
        val propertyCompliance =
            PropertyComplianceBuilder()
                .withElectricalSafety(expiryDate = LocalDate.of(2030, 6, 1), certType = CertificateType.Eic)
                .build()

        assertEquals(LocalDate.of(2030, 6, 1), service.expiryDateFrom(propertyCompliance))
    }

    @Test
    fun `issueDateFrom returns null when no expiry date is stored`() {
        val propertyCompliance = PropertyComplianceBuilder().build()

        assertNull(service.issueDateFrom(propertyCompliance))
    }

    @Test
    fun `applyIssueDate stores an expiry date five years later`() {
        val propertyCompliance = PropertyComplianceBuilder().build()

        service.applyIssueDate(propertyCompliance, LocalDate.of(2025, 6, 1))

        assertEquals(LocalDate.of(2030, 6, 1), propertyCompliance.electricalSafetyExpiryDate)
    }

    @Test
    fun `applyExpiryDate stores the expiry date unchanged`() {
        val propertyCompliance = PropertyComplianceBuilder().build()

        service.applyExpiryDate(propertyCompliance, LocalDate.of(2030, 6, 1))

        assertEquals(LocalDate.of(2030, 6, 1), propertyCompliance.electricalSafetyExpiryDate)
    }

    @Test
    fun `applyIssueDate stores null when given null`() {
        val propertyCompliance =
            PropertyComplianceBuilder().withElectricalSafety(certType = CertificateType.Eic).build()

        service.applyIssueDate(propertyCompliance, null)

        assertNull(propertyCompliance.electricalSafetyExpiryDate)
    }
}
