package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import java.time.LocalDate

class PropertyComplianceBuilder {
    private val propertyCompliance = PropertyCompliance()

    fun build() = propertyCompliance

    fun withGasSafetyCert(issueDate: LocalDate = LocalDate.now()): PropertyComplianceBuilder {
        propertyCompliance.gasSafetyCertS3Key = "gas-key"
        propertyCompliance.gasSafetyCertIssueDate = issueDate
        propertyCompliance.gasSafetyCertEngineerNum = "1234567"
        return this
    }

    fun withExpiredGasSafetyCert(): PropertyComplianceBuilder {
        propertyCompliance.gasSafetyCertIssueDate = LocalDate.now().minusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong())
        return this
    }

    fun withGasSafetyCertExemption(): PropertyComplianceBuilder {
        propertyCompliance.gasSafetyCertExemptionReason = GasSafetyExemptionReason.NO_GAS_SUPPLY
        return this
    }

    fun withEicr(issueDate: LocalDate = LocalDate.now()): PropertyComplianceBuilder {
        propertyCompliance.eicrS3Key = "eicr-key"
        propertyCompliance.eicrIssueDate = issueDate
        return this
    }

    fun withExpiredEicr(): PropertyComplianceBuilder {
        propertyCompliance.eicrIssueDate = LocalDate.now().minusYears(EICR_VALIDITY_YEARS.toLong())
        return this
    }

    fun withEicrExemption(): PropertyComplianceBuilder {
        propertyCompliance.eicrExemptionReason = EicrExemptionReason.LIVE_IN_LANDLORD
        return this
    }

    fun withEpc(expiryDate: LocalDate = LocalDate.now().plusYears(1)): PropertyComplianceBuilder {
        propertyCompliance.epcUrl = "epc-url"
        propertyCompliance.epcExpiryDate = expiryDate
        propertyCompliance.epcEnergyRating = "C"
        return this
    }

    fun withExpiredEpc(): PropertyComplianceBuilder {
        propertyCompliance.tenancyStartedBeforeEpcExpiry = false
        return this.withEpc(expiryDate = LocalDate.now().minusYears(1))
    }

    fun withTenancyStartedBeforeEpcExpiry(): PropertyComplianceBuilder {
        propertyCompliance.tenancyStartedBeforeEpcExpiry = true
        return this
    }

    fun withEpcExemption(): PropertyComplianceBuilder {
        propertyCompliance.epcExemptionReason = EpcExemptionReason.LISTED_BUILDING
        return this
    }

    fun withLowEpcRating(): PropertyComplianceBuilder {
        propertyCompliance.epcEnergyRating = "F"
        return this
    }

    fun withMeesExemption(): PropertyComplianceBuilder {
        propertyCompliance.epcMeesExemptionReason = MeesExemptionReason.PROPERTY_DEVALUATION
        return this
    }

    companion object {
        fun createWithInDateCerts() =
            PropertyComplianceBuilder()
                .withGasSafetyCert()
                .withEicr()
                .withEpc()
                .build()

        fun createWithExpiredCerts() =
            PropertyComplianceBuilder()
                .withExpiredGasSafetyCert()
                .withExpiredEicr()
                .withExpiredEpc()
                .build()

        fun createWithCertExemptions() =
            PropertyComplianceBuilder()
                .withGasSafetyCertExemption()
                .withEicrExemption()
                .withEpcExemption()
                .build()

        fun createWithMissingCerts() =
            PropertyComplianceBuilder()
                .build()
    }
}
