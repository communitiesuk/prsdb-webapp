package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Named.named
import org.junit.jupiter.params.provider.Arguments.arguments
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.UploadedFileUrl
import uk.gov.communities.prsdb.webapp.services.UploadService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder

class ElectricalSafetyViewModelFactoryTests : ComplianceViewModelFactoryTests() {
    override fun createRows(
        uploadService: UploadService,
        propertyCompliance: PropertyCompliance,
    ) = ElectricalSafetyViewModelFactory(uploadService).fromEntity(propertyCompliance)

    companion object {
        private val compliant = PropertyComplianceBuilder.createWithInDateCerts()
        private val compliantViaPluralUploads =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(false)
                .withGasSafetyCert()
                .withElectricalSafetyFileUploads()
                .withElectricalSafetyExpiryDate()
                .withElectricalCertType()
                .withEpc()
                .build()
        private val compliantWithEic =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(false)
                .withGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType(CertificateType.Eic)
                .withEpc()
                .build()
        private val compliantWithFileName =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(false)
                .withGasSafetyCert()
                .withElectricalSafety(
                    fileUpload =
                        FileUpload(
                            FileUploadStatus.SCANNED,
                            "property_1_eicr.pdf",
                            "pdf",
                            "etag",
                            "versionId",
                        ).apply { fileName = "my_electrical_report.pdf" },
                ).withElectricalCertType()
                .withEpc()
                .build()
        private val quarantinedUpload =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(false)
                .withGasSafetyCert()
                .withElectricalSafety(
                    fileUpload =
                        FileUpload(
                            FileUploadStatus.QUARANTINED,
                            "property_1_eicr.pdf",
                            "pdf",
                            "etag",
                            "versionId",
                        ).apply { fileName = "pending_report.pdf" },
                ).withElectricalCertType()
                .withEpc()
                .build()
        private val expiredAfterUpload = PropertyComplianceBuilder.createWithElectricalSafetyExpiredAfterUpload()
        private val expiredBeforeUpload = PropertyComplianceBuilder.createWithElectricalSafetyExpiredBeforeUpload()
        private val missing = PropertyComplianceBuilder.createWithMissingCerts()
        private val missingWithNoCertType =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(false)
                .build()

        @JvmStatic
        private fun provideRows() =
            arrayOf(
                arguments(
                    named(
                        "with compliant electrical safety certificate",
                        compliant,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.eicr.certificate",
                            listOf(
                                UploadedFileUrl(
                                    messageKey = "propertyDetails.complianceInformation.electricalSafety.eicr.downloadCertificate",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.expiryDate",
                            compliant.electricalSafetyExpiryDate,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with compliant electrical safety certificate via plural uploads",
                        compliantViaPluralUploads,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.eicr.certificate",
                            listOf(
                                UploadedFileUrl(
                                    messageKey = "propertyDetails.complianceInformation.electricalSafety.eicr.downloadCertificate",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.expiryDate",
                            compliantViaPluralUploads.electricalSafetyExpiryDate,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with compliant EIC certificate",
                        compliantWithEic,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.eic.certificate",
                            listOf(
                                UploadedFileUrl(
                                    messageKey = "propertyDetails.complianceInformation.electricalSafety.eic.downloadCertificate",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.expiryDate",
                            compliantWithEic.electricalSafetyExpiryDate,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with compliant electrical safety certificate with file name",
                        compliantWithFileName,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.eicr.certificate",
                            listOf(
                                UploadedFileUrl(
                                    messageKey = "propertyDetails.complianceInformation.electricalSafety.eicr.downloadCertificate",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.expiryDate",
                            compliantWithFileName.electricalSafetyExpiryDate,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with quarantined electrical safety upload",
                        quarantinedUpload,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.eicr.certificate",
                            listOf(
                                UploadedFileUrl(
                                    messageKey = VIRUS_SCAN_PENDING_WITH_NAME_KEY,
                                    displayName = "pending_report.pdf",
                                ),
                            ),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.expiryDate",
                            quarantinedUpload.electricalSafetyExpiryDate,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with expired after upload electrical safety certificate",
                        expiredAfterUpload,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.eicr.certificate",
                            listOf(
                                UploadedFileUrl(
                                    messageKey =
                                        "propertyDetails.complianceInformation.electricalSafety.eicr.downloadExpiredCertificate",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.expiryDate",
                            expiredAfterUpload.electricalSafetyExpiryDate,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with expired before upload electrical safety certificate",
                        expiredBeforeUpload,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.eicr.certificate",
                            "propertyDetails.complianceInformation.expired",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.expiryDate",
                            expiredBeforeUpload.electricalSafetyExpiryDate,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "without electrical safety certificate",
                        missing,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.eicr.certificate",
                            "propertyDetails.complianceInformation.notAdded",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.exemption",
                            "propertyDetails.complianceInformation.noExemption",
                        ),
                    ),
                ),
                arguments(
                    named(
                        "without electrical safety certificate and no cert type",
                        missingWithNoCertType,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.certificate",
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
