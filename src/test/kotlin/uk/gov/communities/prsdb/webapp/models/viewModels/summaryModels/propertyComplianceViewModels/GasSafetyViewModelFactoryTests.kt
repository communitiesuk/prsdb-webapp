package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.TagValue
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.UploadedFileUrl
import uk.gov.communities.prsdb.webapp.services.UploadService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class GasSafetyViewModelFactoryTests : ComplianceViewModelFactoryTests() {
    private val gasSafetyViewModelFactory = GasSafetyViewModelFactory(mock(), mock())

    override fun createRows(
        uploadService: UploadService,
        propertyCompliance: PropertyCompliance,
    ): List<SummaryListRowViewModel> {
        val messageSource = mock<MessageSource>()
        whenever(messageSource.getMessage(eq(PROVIDE_LATER_WITH_DEADLINE_KEY), any(), any<Locale>()))
            .thenAnswer { invocation ->
                val args = invocation.getArgument<Array<Any>>(1)
                "Provide this later (before ${args[0]})"
            }
        return GasSafetyViewModelFactory(uploadService, messageSource).fromEntity(propertyCompliance)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInsetTextKeys")
    fun `getInsetTextKey returns the correct key`(
        propertyCompliance: PropertyCompliance,
        expectedKey: String?,
    ) {
        val insetTextKey = gasSafetyViewModelFactory.getInsetTextKey(propertyCompliance)

        assertEquals(expectedKey, insetTextKey)
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
        private const val PROVIDE_LATER_WITH_DEADLINE_KEY = "checkGasSafety.provideThisLater.occupiedWithDeadline"

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
        private val noGasSupply =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(false)
                .withHasGasSupply(false)
                .build()
        private val missingUnoccupied =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(false)
                .withHasGasSupply(true)
                .build()
        private val missingOccupiedProvideLater =
            PropertyComplianceBuilder()
                .withOccupiedPropertyOwnership(lastOccupiedDate = LocalDate.now().minusDays(5))
                .withHasGasSupply(true)
                .withGasSafetyCertProvideLater()
                .build()
        private val missingOccupiedNoCert =
            PropertyComplianceBuilder()
                .withOccupiedPropertyOwnership()
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
        private val expiredOccupied =
            PropertyComplianceBuilder()
                .withOccupiedPropertyOwnership()
                .withHasGasSupply(true)
                .withExpiredGasSafetyCert()
                .withElectricalSafety()
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
                            "propertyDetails.complianceInformation.certificateStatus",
                            TagValue("propertyDetails.complianceInformation.valid", "green"),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.hasValidCert",
                            "commonText.yes",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            compliant.gasSafetyCertIssueDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.yourCertificate",
                            listOf(
                                UploadedFileUrl(
                                    messageKey = "propertyDetails.complianceInformation.gasSafety.downloadCertificate",
                                    displayName = "gas_safety_certificate.pdf",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
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
                            "propertyDetails.complianceInformation.certificateStatus",
                            TagValue("propertyDetails.complianceInformation.valid", "green"),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.hasValidCert",
                            "commonText.yes",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            compliantViaPluralUploads.gasSafetyCertIssueDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.yourCertificate",
                            listOf(
                                UploadedFileUrl(
                                    messageKey = "propertyDetails.complianceInformation.gasSafety.downloadCertificate",
                                    displayName = "gas_safety_certificate.pdf",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
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
                            "propertyDetails.complianceInformation.certificateStatus",
                            TagValue("propertyDetails.complianceInformation.valid", "green"),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.hasValidCert",
                            "commonText.yes",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            compliantWithFileName.gasSafetyCertIssueDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.yourCertificate",
                            listOf(
                                UploadedFileUrl(
                                    messageKey = "propertyDetails.complianceInformation.gasSafety.downloadCertificate",
                                    displayName = "my_gas_certificate.pdf",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
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
                            "propertyDetails.complianceInformation.certificateStatus",
                            TagValue("propertyDetails.complianceInformation.valid", "green"),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.hasValidCert",
                            "commonText.yes",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            quarantinedUpload.gasSafetyCertIssueDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.yourCertificate",
                            listOf(
                                UploadedFileUrl(
                                    messageKey = VIRUS_SCAN_PENDING_WITH_NAME_KEY,
                                    displayName = "pending_gas.pdf",
                                ),
                            ),
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
                            "propertyDetails.complianceInformation.certificateStatus",
                            TagValue("propertyDetails.complianceInformation.expired", "red"),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.hasValidCert",
                            "commonText.yes",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            expiredAfterUpload.gasSafetyCertIssueDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.yourCertificate",
                            listOf(
                                UploadedFileUrl(
                                    messageKey = "propertyDetails.complianceInformation.gasSafety.downloadExpiredCertificate",
                                    displayName = "gas_safety_certificate.pdf",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
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
                            "propertyDetails.complianceInformation.certificateStatus",
                            TagValue("propertyDetails.complianceInformation.expired", "red"),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.hasValidCert",
                            "commonText.yes",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            expiredBeforeUpload.gasSafetyCertIssueDate,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "without gas safety certificate and unoccupied",
                        missingUnoccupied,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.hasGasSupply",
                            "commonText.yes",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.hasValidCert",
                            "checkGasSafety.provideThisLater.unoccupied",
                        ),
                    ),
                ),
                arguments(
                    named(
                        "without gas safety certificate, occupied, and provide later",
                        missingOccupiedProvideLater,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.hasGasSupply",
                            "commonText.yes",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.hasValidCert",
                            "Provide this later (before ${
                                missingOccupiedProvideLater.propertyOwnership.lastOccupiedDate
                                    ?.plusDays(PROVIDE_LATER_DEADLINE_DAYS)
                                    ?.format(DATE_FORMATTER)
                            })",
                        ),
                    ),
                ),
                arguments(
                    named(
                        "without gas safety certificate and occupied",
                        missingOccupiedNoCert,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.hasGasSupply",
                            "commonText.yes",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.hasValidCert",
                            "commonText.no",
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with no gas supply",
                        noGasSupply,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.hasGasSupply",
                            "commonText.no",
                        ),
                    ),
                ),
            )

        @JvmStatic
        private fun provideInsetTextKeys() =
            arrayOf(
                arguments(named("with compliant gas cert", compliant), null),
                arguments(named("without gas cert and unoccupied", missingUnoccupied), null),
                arguments(
                    named("without gas cert and occupied (no cert)", missingOccupiedNoCert),
                    "checkGasSafety.occupiedNoCertInsetText",
                ),
                arguments(named("without gas cert and occupied (provide later)", missingOccupiedProvideLater), null),
                arguments(named("with no gas supply", noGasSupply), "checkGasSafety.noGasSupplyInsetText"),
                arguments(
                    named("with expired gas cert and occupied", expiredOccupied),
                    "checkGasSafety.occupiedNoCertInsetText",
                ),
                arguments(named("with expired gas cert and unoccupied", expiredBeforeUpload), null),
            )
    }
}
