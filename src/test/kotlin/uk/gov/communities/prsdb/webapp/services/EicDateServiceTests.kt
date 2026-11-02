package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTest
import uk.gov.communities.prsdb.webapp.constants.EIC_ISSUE_DATE
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import java.time.LocalDate

class EicDateServiceTests : FeatureFlagTest() {
    @Autowired
    lateinit var service: EicDateService

    // TODO PDJB-1669: Update expectations for issue-date-backed storage when the flag is enabled.
    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `both flag states preserve expiry-backed date reads and writes`(enabled: Boolean) {
        if (enabled) {
            featureFlagManager.enableFeature(EIC_ISSUE_DATE)
        } else {
            featureFlagManager.disableFeature(EIC_ISSUE_DATE)
        }
        val propertyCompliance = PropertyComplianceBuilder().withElectricalSafety(certType = CertificateType.Eic).build()
        val issueDate = LocalDate.of(2025, 6, 1)
        val expiryDate = LocalDate.of(2030, 6, 1)

        service.applyIssueDate(propertyCompliance, issueDate)

        assertEquals(expiryDate, propertyCompliance.electricalSafetyExpiryDate)
        assertEquals(issueDate, service.issueDateFrom(propertyCompliance))
        assertEquals(expiryDate, service.expiryDateFrom(propertyCompliance))

        service.applyExpiryDate(propertyCompliance, expiryDate.plusDays(1))

        assertEquals(issueDate.plusDays(1), service.issueDateFrom(propertyCompliance))
        assertEquals(expiryDate.plusDays(1), service.expiryDateFrom(propertyCompliance))

        service.applyIssueDate(propertyCompliance, null)

        assertNull(service.issueDateFrom(propertyCompliance))
        assertNull(service.expiryDateFrom(propertyCompliance))
    }
}
