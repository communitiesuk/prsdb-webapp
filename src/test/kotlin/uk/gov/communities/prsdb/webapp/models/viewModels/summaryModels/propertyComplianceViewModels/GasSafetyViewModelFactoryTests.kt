package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Named.named
import org.junit.jupiter.params.provider.Arguments.arguments
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.UploadedFileUrl
import uk.gov.communities.prsdb.webapp.services.UploadService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder

class GasSafetyViewModelFactoryTests : ComplianceViewModelFactoryTests() {
    override fun createRows(
        uploadService: UploadService,
        propertyCompliance: PropertyCompliance,
    ) = GasSafetyViewModelFactory(uploadService).fromEntity(propertyCompliance)

    companion object {
        private val compliant =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(false)
                .withHasGasSupply(true)
                .withGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withEpc()
                .build()
        private val compliantViaPluralUploads =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(false)
                .withHasGasSupply(true)
                .withGasSafetyCert(fileUpload = null)
                .withGasSafetyFileUploads()
                .withElectricalSafety()
                .withElectricalCertType()
                .withEpc()
                .build()
        private val expiredAfterUpload =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(false)
                .withHasGasSupply(true)
                .withGasSafetyCert()
                .withExpiredGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withEpc()
                .build()
        private val expiredBeforeUpload =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(false)
                .withHasGasSupply(true)
                .withExpiredGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withEpc()
                .build()
        private val exempt =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(false)
                .withHasGasSupply(false)
                .withGasSafetyCertExemption(GasSafetyExemptionReason.NO_GAS_SUPPLY)
                .withEicrExemption()
                .withEpcExemption()
                .build()
        private val missing =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(false)
                .withHasGasSupply(true)
                .build()
        private val compliantWithFileName =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(false)
                .withHasGasSupply(true)
                .withGasSafetyCert(
                    fileUpload =
                        FileUpload(
                            FileUploadStatus.SCANNED,
                            "property_1_gas.pdf",
                            "pdf",
                            "etag",
                            "versionId",
                        ).apply { fileName = "my_gas_certificate.pdf" },
                ).withElectricalSafety()
                .withElectricalCertType()
                .withEpc()
                .build()
        private val quarantinedUpload =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(false)
                .withHasGasSupply(true)
                .withGasSafetyCert(
                    fileUpload =
                        FileUpload(
                            FileUploadStatus.QUARANTINED,
                            "property_1_gas.pdf",
                            "pdf",
                            "etag",
                            "versionId",
                        ).apply { fileName = "pending_gas.pdf" },
                ).withElectricalSafety()
                .withElectricalCertType()
                .withEpc()
                .build()

        @JvmStatic
        private fun provideRows() =
            arrayOf(
                arguments(
                    named(
                        "with compliant gas safety certificate",
                        compliant,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafetyCertificate",
                            listOf(
                                UploadedFileUrl(
                                    messageKey = "propertyDetails.complianceInformation.gasSafety.downloadCertificate",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            compliant.gasSafetyCertIssueDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafeEngineerNumber",
                            compliant.gasSafetyCertEngineerNum,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with compliant gas safety certificate via plural uploads",
                        compliantViaPluralUploads,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafetyCertificate",
                            listOf(
                                UploadedFileUrl(
                                    messageKey = "propertyDetails.complianceInformation.gasSafety.downloadCertificate",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            compliantViaPluralUploads.gasSafetyCertIssueDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafeEngineerNumber",
                            compliantViaPluralUploads.gasSafetyCertEngineerNum,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with compliant gas safety certificate with file name",
                        compliantWithFileName,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafetyCertificate",
                            listOf(
                                UploadedFileUrl(
                                    messageKey = "propertyDetails.complianceInformation.gasSafety.downloadCertificate",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            compliantWithFileName.gasSafetyCertIssueDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafeEngineerNumber",
                            compliantWithFileName.gasSafetyCertEngineerNum,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with quarantined gas safety upload",
                        quarantinedUpload,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafetyCertificate",
                            listOf(
                                UploadedFileUrl(
                                    messageKey = VIRUS_SCAN_PENDING_WITH_NAME_KEY,
                                    displayName = "pending_gas.pdf",
                                ),
                            ),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            quarantinedUpload.gasSafetyCertIssueDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafeEngineerNumber",
                            quarantinedUpload.gasSafetyCertEngineerNum,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with expired after upload gas safety certificate",
                        expiredAfterUpload,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafetyCertificate",
                            listOf(
                                UploadedFileUrl(
                                    messageKey = "propertyDetails.complianceInformation.gasSafety.downloadExpiredCertificate",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            expiredAfterUpload.gasSafetyCertIssueDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafeEngineerNumber",
                            expiredAfterUpload.gasSafetyCertEngineerNum,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with expired before upload gas safety certificate",
                        expiredBeforeUpload,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafetyCertificate",
                            "propertyDetails.complianceInformation.expired",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            expiredBeforeUpload.gasSafetyCertIssueDate,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with gas safety exemption",
                        exempt,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafetyCertificate",
                            "propertyDetails.complianceInformation.exempt",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.exemption",
                            MessageKeyConverter.convert(GasSafetyExemptionReason.NO_GAS_SUPPLY),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "without gas safety certificate",
                        missing,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafetyCertificate",
                            "propertyDetails.complianceInformation.notAdded",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.exemption",
                            "propertyDetails.complianceInformation.noExemption",
                        ),
                    ),
                ),
            )
    }
}
