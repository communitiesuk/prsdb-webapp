package uk.gov.communities.prsdb.webapp.testHelpers.builders

import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.LocalDate

class PropertyComplianceBuilder {
    private val propertyCompliance = PropertyCompliance()

    fun build() = propertyCompliance

    fun withPropertyOwnership(
        propertyOwnership: PropertyOwnership = MockLandlordData.createPropertyOwnership(),
    ): PropertyComplianceBuilder {
        ReflectionTestUtils.setField(propertyCompliance, "propertyOwnership", propertyOwnership)
        return this
    }

    fun withGasSafetyCert(issueDate: LocalDate = LocalDate.now()): PropertyComplianceBuilder {
        propertyCompliance.gasSafetyFileUpload = FileUpload(FileUploadStatus.QUARANTINED, "property_1_gas_safety_certificate", "pdf")
        propertyCompliance.gasSafetyCertIssueDate = issueDate
        propertyCompliance.gasSafetyCertEngineerNum = "1234567"
        return this
    }

    fun withExpiredGasSafetyCert(): PropertyComplianceBuilder {
        propertyCompliance.gasSafetyCertIssueDate = LocalDate.now().minusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong())
        return this
    }

    fun withGasSafetyCertExemption(
        exemption: GasSafetyExemptionReason = GasSafetyExemptionReason.NO_GAS_SUPPLY,
    ): PropertyComplianceBuilder {
        propertyCompliance.gasSafetyCertExemptionReason = exemption
        return this
    }

    fun withEicr(issueDate: LocalDate = LocalDate.now()): PropertyComplianceBuilder {
        propertyCompliance.eicrFileUpload = FileUpload(FileUploadStatus.QUARANTINED, "property_1_eicr.pdf", "pdf")
        propertyCompliance.eicrIssueDate = issueDate
        return this
    }

    fun withExpiredEicr(): PropertyComplianceBuilder {
        propertyCompliance.eicrIssueDate = LocalDate.now().minusYears(EICR_VALIDITY_YEARS.toLong())
        return this
    }

    fun withEicrExemption(exemption: EicrExemptionReason = EicrExemptionReason.LIVE_IN_LANDLORD): PropertyComplianceBuilder {
        propertyCompliance.eicrExemptionReason = exemption
        return this
    }

    fun withEpc(expiryDate: LocalDate = LocalDate.now().plusYears(1)): PropertyComplianceBuilder {
        propertyCompliance.epcUrl = "$TEST_EPC_BASE_URL/0000-0000-0000-0000-0000"
        propertyCompliance.epcExpiryDate = expiryDate
        if (expiryDate.isBefore(LocalDate.now())) propertyCompliance.tenancyStartedBeforeEpcExpiry = false
        propertyCompliance.epcEnergyRating = "C"
        return this
    }

    fun withExpiredEpc(): PropertyComplianceBuilder = this.withEpc(expiryDate = LocalDate.now().minusYears(1))

    fun withTenancyStartedBeforeEpcExpiry(): PropertyComplianceBuilder {
        propertyCompliance.tenancyStartedBeforeEpcExpiry = true
        return this
    }

    fun withEpcExemption(exemption: EpcExemptionReason = EpcExemptionReason.LISTED_BUILDING): PropertyComplianceBuilder {
        propertyCompliance.epcExemptionReason = exemption
        return this
    }

    fun withLowEpcRating(): PropertyComplianceBuilder {
        propertyCompliance.epcEnergyRating = "F"
        return this
    }

    fun withMeesExemption(exemption: MeesExemptionReason = MeesExemptionReason.PROPERTY_DEVALUATION): PropertyComplianceBuilder {
        propertyCompliance.epcMeesExemptionReason = exemption
        return this
    }

    companion object {
        fun createWithInDateCerts() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withGasSafetyCert()
                .withEicr()
                .withEpc()
                .build()

        fun createWithInDateCertsAndLowEpcRating() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withGasSafetyCert()
                .withEicr()
                .withEpc()
                .withLowEpcRating()
                .build()

        fun createWithInDateCertsAndLowEpcRatingAndMeesExemptionReason(
            exemption: MeesExemptionReason = MeesExemptionReason.PROPERTY_DEVALUATION,
        ) = PropertyComplianceBuilder()
            .withPropertyOwnership()
            .withGasSafetyCert()
            .withEicr()
            .withEpc()
            .withMeesExemption(exemption)
            .withLowEpcRating()
            .build()

        fun createWithExpiredCerts() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withExpiredGasSafetyCert()
                .withExpiredEicr()
                .withExpiredEpc()
                .build()

        fun createWithNaturallyExpiredCerts() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withGasSafetyCert(issueDate = LocalDate.now().minusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong() + 1))
                .withEicr(issueDate = LocalDate.now().minusYears(EICR_VALIDITY_YEARS.toLong() + 1))
                .withEpc()
                .build()

        fun createWithGasAndEicrExpiredCerts() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withExpiredGasSafetyCert()
                .withExpiredEicr()
                .withEpc()
                .build()

        fun createWithGasAndEpcExpiredCerts() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withExpiredGasSafetyCert()
                .withEicr()
                .withExpiredEpc()
                .build()

        fun createWithEicrAndEpcExpiredCerts() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withGasSafetyCert()
                .withExpiredEicr()
                .withExpiredEpc()
                .build()

        fun createWithGasCertExpiredBeforeUpload() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withExpiredGasSafetyCert()
                .withEicr()
                .withEpc()
                .build()

        fun createWithEicrExpiredBeforeUpload() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withGasSafetyCert()
                .withExpiredEicr()
                .withEpc()
                .build()

        fun createWithGasCertExpiredAfterUpload() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withGasSafetyCert()
                .withExpiredGasSafetyCert()
                .withEicr()
                .withEpc()
                .build()

        fun createWithEicrExpiredAfterUpload() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withGasSafetyCert()
                .withEicr()
                .withExpiredEicr()
                .withEpc()
                .build()

        fun createWithOnlyEpcExpiredCert() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withGasSafetyCert()
                .withEicr()
                .withExpiredEpc()
                .build()

        fun createWithCertExemptions(
            gasExemption: GasSafetyExemptionReason = GasSafetyExemptionReason.NO_GAS_SUPPLY,
            eicrExemption: EicrExemptionReason = EicrExemptionReason.LIVE_IN_LANDLORD,
            epcExemption: EpcExemptionReason = EpcExemptionReason.LISTED_BUILDING,
        ) = PropertyComplianceBuilder()
            .withPropertyOwnership()
            .withGasSafetyCertExemption(gasExemption)
            .withEicrExemption(eicrExemption)
            .withEpcExemption(epcExemption)
            .build()

        fun createWithMissingCerts() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .build()

        fun createWithGasAndEicrMissingCerts() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withEpc()
                .build()

        fun createWithGasAndEpcMissingCerts() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withEicr()
                .build()

        fun createWithEicrAndEpcMissingCerts() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withGasSafetyCert()
                .build()

        fun createWithOnlyGasMissingCert() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withEicr()
                .withEpc()
                .build()

        fun createWithOnlyEicrMissingCert() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withGasSafetyCert()
                .withEpc()
                .build()

        fun createWithOnlyEpcMissingCert() =
            PropertyComplianceBuilder()
                .withPropertyOwnership()
                .withGasSafetyCert()
                .withEicr()
                .build()

        const val TEST_EPC_BASE_URL = "epc-url"
    }
}
