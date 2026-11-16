package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import java.time.LocalDate

class AfterPdjb1666EicDateServiceTests {
    private val service = AfterPdjb1666EicDateService()

    private fun eicWithIssueDate(issueDate: LocalDate?) =
        PropertyComplianceBuilder().withElectricalCertType(CertificateType.Eic).build().apply { eicIssueDate = issueDate }

    @Test
    fun `issueDateFrom returns the stored issue date`() {
        assertEquals(LocalDate.of(2025, 6, 1), service.issueDateFrom(eicWithIssueDate(LocalDate.of(2025, 6, 1))))
    }

    @Test
    fun `expiryDateFrom returns five years after the stored issue date`() {
        assertEquals(LocalDate.of(2030, 6, 1), service.expiryDateFrom(eicWithIssueDate(LocalDate.of(2025, 6, 1))))
    }

    @Test
    fun `expiryDateFrom returns null when no issue date is stored`() {
        assertNull(service.expiryDateFrom(eicWithIssueDate(null)))
    }

    @Test
    fun `applyIssueDate stores the issue date unchanged`() {
        val propertyCompliance = PropertyComplianceBuilder().build()

        service.applyIssueDate(propertyCompliance, LocalDate.of(2025, 6, 1))

        assertEquals(LocalDate.of(2025, 6, 1), propertyCompliance.eicIssueDate)
        assertNull(propertyCompliance.eicrExpiryDate)
    }

    @Test
    fun `applyExpiryDate stores an issue date five years earlier`() {
        val propertyCompliance = PropertyComplianceBuilder().build()

        service.applyExpiryDate(propertyCompliance, LocalDate.of(2030, 6, 1))

        assertEquals(LocalDate.of(2025, 6, 1), propertyCompliance.eicIssueDate)
        assertNull(propertyCompliance.eicrExpiryDate)
    }

    @Test
    fun `applyExpiryDate stores null when given null`() {
        val propertyCompliance = eicWithIssueDate(LocalDate.of(2025, 6, 1))

        service.applyExpiryDate(propertyCompliance, null)

        assertNull(propertyCompliance.eicIssueDate)
    }
}
