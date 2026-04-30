package uk.gov.communities.prsdb.webapp.testHelpers.builders

import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.constants.ELECTRICAL_SAFETY_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
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

    fun withOccupiedPropertyOwnership(): PropertyComplianceBuilder {
        ReflectionTestUtils.setField(propertyCompliance, "propertyOwnership", MockLandlordData.createOccupiedPropertyOwnership())
        return this
    }

    fun withUnoccupiedPropertyOwnership(): PropertyComplianceBuilder {
        ReflectionTestUtils.setField(propertyCompliance, "propertyOwnership", MockLandlordData.createUnoccupiedPropertyOwnership())
        return this
    }

    fun withPropertyOwnershipWithOccupancy(isOccupied: Boolean) =
        when (isOccupied) {
            true -> withOccupiedPropertyOwnership()
            false -> withUnoccupiedPropertyOwnership()
        }

    fun withHasGasSupply(hasGasSupply: Boolean = true): PropertyComplianceBuilder {
        propertyCompliance.hasGasSupply = hasGasSupply
        return this
    }

    fun withGasSafetyCert(
        issueDate: LocalDate = LocalDate.now(),
        engineerNum: String? = "1234567",
        fileUpload: FileUpload? =
            FileUpload(
                FileUploadStatus.SCANNED,
                "property_1_gas_safety_certificate",
                "pdf",
                "etag",
                "versionId",
            ),
    ): PropertyComplianceBuilder {
        propertyCompliance.gasSafetyFileUpload = fileUpload
        if (fileUpload != null) {
            propertyCompliance.gasSafetyFileUploads = mutableListOf(fileUpload)
        }
        propertyCompliance.gasSafetyCertIssueDate = issueDate
        propertyCompliance.gasSafetyCertEngineerNum = engineerNum
        return this
    }

    fun withGasSafetyFileUploads(
        fileUploads: List<FileUpload> =
            listOf(
                FileUpload(
                    FileUploadStatus.SCANNED,
                    "property_1_gas_safety_certificate",
                    "pdf",
                    "etag",
                    "versionId",
                ),
            ),
    ): PropertyComplianceBuilder {
        propertyCompliance.gasSafetyFileUploads = fileUploads.toMutableList()
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

    fun withGasSafetyCertOtherExemption(otherExemptionReason: String = "Other reason"): PropertyComplianceBuilder {
        propertyCompliance.gasSafetyCertExemptionReason = GasSafetyExemptionReason.OTHER
        propertyCompliance.gasSafetyCertExemptionOtherReason = otherExemptionReason
        return this
    }

    // Combined convenience method for Electrical Safety
    fun withElectricalSafety(
        issueDate: LocalDate = LocalDate.now(),
        expiryDate: LocalDate = issueDate.plusYears(ELECTRICAL_SAFETY_VALIDITY_YEARS.toLong()),
        fileUpload: FileUpload =
            FileUpload(
                FileUploadStatus.SCANNED,
                "property_1_eicr.pdf",
                "pdf",
                "etag",
                "versionId",
            ),
    ): PropertyComplianceBuilder {
        propertyCompliance.eicrFileUpload = fileUpload
        propertyCompliance.electricalSafetyFileUploads = mutableListOf(fileUpload)
        propertyCompliance.eicrIssueDate = issueDate
        propertyCompliance.electricalSafetyExpiryDate = expiryDate
        return this
    }

    fun withElectricalSafetyFileUploads(
        fileUploads: List<FileUpload> =
            listOf(
                FileUpload(
                    FileUploadStatus.SCANNED,
                    "property_1_eicr",
                    "pdf",
                    "etag",
                    "versionId",
                ),
            ),
    ): PropertyComplianceBuilder {
        propertyCompliance.electricalSafetyFileUploads = fileUploads.toMutableList()
        return this
    }

    fun withElectricalSafetyExpiryDate(
        expiryDate: LocalDate = LocalDate.now().plusYears(ELECTRICAL_SAFETY_VALIDITY_YEARS.toLong()),
    ): PropertyComplianceBuilder {
        propertyCompliance.electricalSafetyExpiryDate = expiryDate
        return this
    }

    fun withExpiredElectricalSafety(): PropertyComplianceBuilder {
        val issueDate = LocalDate.now().minusYears(ELECTRICAL_SAFETY_VALIDITY_YEARS.toLong())
        propertyCompliance.eicrIssueDate = issueDate
        propertyCompliance.electricalSafetyExpiryDate = issueDate.plusYears(ELECTRICAL_SAFETY_VALIDITY_YEARS.toLong())
        return this
    }

    fun withElectricalCertType(certType: CertificateType = CertificateType.Eicr): PropertyComplianceBuilder {
        propertyCompliance.electricalCertType = certType
        return this
    }

    fun withEicrExemption(exemption: EicrExemptionReason = EicrExemptionReason.LONG_LEASE): PropertyComplianceBuilder {
        propertyCompliance.eicrExemptionReason = exemption
        return this
    }

    fun withEicrOtherExemption(otherExemptionReason: String = "Other reason"): PropertyComplianceBuilder {
        propertyCompliance.eicrExemptionReason = EicrExemptionReason.OTHER
        propertyCompliance.eicrExemptionOtherReason = otherExemptionReason
        return this
    }

    // Individual EPC setters
    fun withTenancyStartedBeforeEpcExpiry(started: Boolean? = true): PropertyComplianceBuilder {
        propertyCompliance.tenancyStartedBeforeEpcExpiry = started
        return this
    }

    fun withEpc(
        expiryDate: LocalDate = LocalDate.now().plusYears(1),
        energyRating: String = "C",
        epcUrl: String = "$TEST_EPC_BASE_URL/0000-0000-0000-0000-0000",
    ): PropertyComplianceBuilder {
        propertyCompliance.epcUrl = epcUrl
        propertyCompliance.epcExpiryDate = expiryDate
        if (expiryDate.isBefore(LocalDate.now())) propertyCompliance.tenancyStartedBeforeEpcExpiry = false
        propertyCompliance.epcEnergyRating = energyRating
        return this
    }

    fun withExpiredEpc(): PropertyComplianceBuilder = this.withEpc(expiryDate = LocalDate.now().minusYears(1))

    fun withLowEpcRating(): PropertyComplianceBuilder {
        propertyCompliance.epcEnergyRating = "F"
        return this
    }

    fun withEpcExemption(exemption: EpcExemptionReason = EpcExemptionReason.DUE_FOR_DEMOLITION): PropertyComplianceBuilder {
        propertyCompliance.epcExemptionReason = exemption
        return this
    }

    fun withMeesExemption(exemption: MeesExemptionReason = MeesExemptionReason.PROPERTY_DEVALUATION): PropertyComplianceBuilder {
        propertyCompliance.epcMeesExemptionReason = exemption
        return this
    }

    companion object {
        fun createWithInDateCerts(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withEpc()
                .build()

        fun createWithInDateCertsAndLowEpcRating(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withEpc()
                .withLowEpcRating()
                .build()

        fun createWithInDateCertsAndLowEpcRatingAndMeesExemptionReason(
            exemption: MeesExemptionReason = MeesExemptionReason.PROPERTY_DEVALUATION,
            propertyIsOccupied: Boolean = false,
        ) = PropertyComplianceBuilder()
            .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
            .withGasSafetyCert()
            .withElectricalSafety()
            .withElectricalCertType()
            .withEpc()
            .withMeesExemption(exemption)
            .withLowEpcRating()
            .build()

        fun createWithExpiredCerts(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withExpiredGasSafetyCert()
                .withExpiredElectricalSafety()
                .withElectricalCertType()
                .withExpiredEpc()
                .build()

        fun createWithExpiredCertsAndLowEpcRating(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withExpiredGasSafetyCert()
                .withExpiredElectricalSafety()
                .withElectricalCertType()
                .withExpiredEpc()
                .withLowEpcRating()
                .build()

        fun createWithNaturallyExpiredCerts(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withGasSafetyCert(issueDate = LocalDate.now().minusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong() + 1))
                .withElectricalSafety(issueDate = LocalDate.now().minusYears(ELECTRICAL_SAFETY_VALIDITY_YEARS.toLong() + 1))
                .withElectricalCertType()
                .withEpc()
                .build()

        fun createWithGasAndElectricalSafetyExpiredCerts(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withExpiredGasSafetyCert()
                .withExpiredElectricalSafety()
                .withElectricalCertType()
                .withEpc()
                .build()

        fun createWithGasAndEpcExpiredCerts(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withExpiredGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withExpiredEpc()
                .build()

        fun createWithElectricalSafetyAndEpcExpiredCerts(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withGasSafetyCert()
                .withExpiredElectricalSafety()
                .withElectricalCertType()
                .withExpiredEpc()
                .build()

        fun createWithGasCertExpiredBeforeUpload(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withExpiredGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withEpc()
                .build()

        fun createWithElectricalSafetyExpiredBeforeUpload(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withGasSafetyCert()
                .withExpiredElectricalSafety()
                .withElectricalCertType()
                .withEpc()
                .build()

        fun createWithGasCertExpiredAfterUpload(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withGasSafetyCert()
                .withExpiredGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withEpc()
                .build()

        fun createWithElectricalSafetyExpiredAfterUpload(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withGasSafetyCert()
                .withElectricalSafety()
                .withExpiredElectricalSafety()
                .withElectricalCertType()
                .withEpc()
                .build()

        fun createWithOnlyEpcExpiredCert(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withExpiredEpc()
                .build()

        fun createWithGasElectricMissingAndEpcLowEnergy(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withElectricalCertType()
                .withEpc()
                .withLowEpcRating()
                .build()

        fun createWithCertExemptions(
            gasExemption: GasSafetyExemptionReason = GasSafetyExemptionReason.NO_GAS_SUPPLY,
            eicrExemption: EicrExemptionReason = EicrExemptionReason.LONG_LEASE,
            epcExemption: EpcExemptionReason = EpcExemptionReason.DUE_FOR_DEMOLITION,
            propertyIsOccupied: Boolean = false,
        ) = PropertyComplianceBuilder()
            .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
            .withGasSafetyCertExemption(gasExemption)
            .withEicrExemption(eicrExemption)
            .withEpcExemption(epcExemption)
            .build()

        fun createWithMissingCerts(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withElectricalCertType()
                .build()

        fun createWithGasAndElectricalSafetyMissingCerts(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withElectricalCertType()
                .withEpc()
                .build()

        fun createWithGasAndEpcMissingCerts(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withElectricalSafety()
                .withElectricalCertType()
                .build()

        fun createWithElectricalSafetyAndEpcMissingCerts(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withElectricalCertType()
                .withGasSafetyCert()
                .build()

        fun createWithOnlyGasMissingCert(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withElectricalSafety()
                .withElectricalCertType()
                .withEpc()
                .build()

        fun createWithOnlyElectricalSafetyMissingCert(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withElectricalCertType()
                .withGasSafetyCert()
                .withEpc()
                .build()

        fun createWithOnlyEpcMissingCert(propertyIsOccupied: Boolean = false) =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(propertyIsOccupied)
                .withGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .build()

        const val TEST_EPC_BASE_URL = "epc-url"
    }
}
