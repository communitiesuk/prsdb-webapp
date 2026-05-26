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
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
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

class ElectricalSafetyViewModelFactoryTests : ComplianceViewModelFactoryTests() {
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
        return ElectricalSafetyViewModelFactory(uploadService, messageSource).fromEntity(propertyCompliance)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInsetTextKeys")
    fun `getInsetTextKey returns the correct key`(
        propertyCompliance: PropertyCompliance,
        expectedKey: String?,
    ) {
        val insetTextKey = electricalSafetyViewModelFactory.getInsetTextKey(propertyCompliance)

        assertEquals(expectedKey, insetTextKey)
    }

    companion object {
        private val mockMessageSource: MessageSource = mock()
        private val mockUploadService: UploadService = mock()
        private val electricalSafetyViewModelFactory = ElectricalSafetyViewModelFactory(mockUploadService, mockMessageSource)
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
        private const val PROVIDE_LATER_WITH_DEADLINE_KEY = "checkElectricalSafety.provideThisLater.occupiedWithDeadline"

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
        private val expiredAfterUploadOccupied =
            PropertyComplianceBuilder.createWithElectricalSafetyExpiredAfterUpload(
                propertyIsOccupied = true,
            )
        private val expiredBeforeUpload = PropertyComplianceBuilder.createWithElectricalSafetyExpiredBeforeUpload()
        private val missingUnoccupied = PropertyComplianceBuilder.createWithMissingCerts()
        private val missingOccupiedProvideLater =
            PropertyComplianceBuilder()
                .withOccupiedPropertyOwnership(lastOccupiedDate = LocalDate.now().minusDays(5))
                .withElectricalCertType()
                .withElectricalSafetyCertProvideLater()
                .build()
        private val missingOccupiedNoCert =
            PropertyComplianceBuilder()
                .withOccupiedPropertyOwnership()
                .withElectricalCertType()
                .build()
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
                    rowsWithUploads(
                        certKeyPrefix = "propertyDetails.complianceInformation.electricalSafety.eicr",
                        uploadedFiles =
                            listOf(
                                UploadedFileUrl(
                                    messageKey = "propertyDetails.complianceInformation.electricalSafety.eicr.downloadCertificate",
                                    displayName = "electrical_safety_certificate.pdf",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
                        expiryDate = compliant.electricalSafetyExpiryDate,
                    ),
                ),
                arguments(
                    named(
                        "with compliant electrical safety certificate via plural uploads",
                        compliantViaPluralUploads,
                    ),
                    rowsWithUploads(
                        certKeyPrefix = "propertyDetails.complianceInformation.electricalSafety.eicr",
                        uploadedFiles =
                            listOf(
                                UploadedFileUrl(
                                    messageKey = "propertyDetails.complianceInformation.electricalSafety.eicr.downloadCertificate",
                                    displayName = "electrical_safety_certificate.pdf",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
                        expiryDate = compliantViaPluralUploads.electricalSafetyExpiryDate,
                    ),
                ),
                arguments(
                    named(
                        "with compliant EIC certificate",
                        compliantWithEic,
                    ),
                    rowsWithUploads(
                        certKeyPrefix = "propertyDetails.complianceInformation.electricalSafety.eic",
                        uploadedFiles =
                            listOf(
                                UploadedFileUrl(
                                    messageKey = "propertyDetails.complianceInformation.electricalSafety.eic.downloadCertificate",
                                    displayName = "electrical_safety_certificate.pdf",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
                        expiryDate = compliantWithEic.electricalSafetyExpiryDate,
                    ),
                ),
                arguments(
                    named(
                        "with compliant electrical safety certificate with file name",
                        compliantWithFileName,
                    ),
                    rowsWithUploads(
                        certKeyPrefix = "propertyDetails.complianceInformation.electricalSafety.eicr",
                        uploadedFiles =
                            listOf(
                                UploadedFileUrl(
                                    messageKey = "propertyDetails.complianceInformation.electricalSafety.eicr.downloadCertificate",
                                    displayName = "my_electrical_report.pdf",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
                        expiryDate = compliantWithFileName.electricalSafetyExpiryDate,
                    ),
                ),
                arguments(
                    named(
                        "with quarantined electrical safety upload",
                        quarantinedUpload,
                    ),
                    rowsWithUploads(
                        certKeyPrefix = "propertyDetails.complianceInformation.electricalSafety.eicr",
                        uploadedFiles =
                            listOf(
                                UploadedFileUrl(
                                    messageKey = VIRUS_SCAN_PENDING_WITH_NAME_KEY,
                                    displayName = "pending_report.pdf",
                                ),
                            ),
                        expiryDate = quarantinedUpload.electricalSafetyExpiryDate,
                    ),
                ),
                arguments(
                    named(
                        "with expired after upload electrical safety certificate",
                        expiredAfterUpload,
                    ),
                    rowsWithUploads(
                        certKeyPrefix = "propertyDetails.complianceInformation.electricalSafety.eicr",
                        uploadedFiles =
                            listOf(
                                UploadedFileUrl(
                                    messageKey =
                                        "propertyDetails.complianceInformation.electricalSafety.eicr.downloadExpiredCertificate",
                                    displayName = "electrical_safety_certificate.pdf",
                                    url = DOWNLOAD_URL,
                                ),
                            ),
                        expiryDate = expiredAfterUpload.electricalSafetyExpiryDate,
                        isValidCertificate = false,
                    ),
                ),
                arguments(
                    named(
                        "with expired before upload electrical safety certificate",
                        expiredBeforeUpload,
                    ),
                    rowsWithoutUploads(
                        certKeyPrefix = "propertyDetails.complianceInformation.electricalSafety.eicr",
                        expiryDate = expiredBeforeUpload.electricalSafetyExpiryDate,
                    ),
                ),
                arguments(
                    named(
                        "without electrical safety certificate and unoccupied",
                        missingUnoccupied,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.hasValidCert",
                            "checkElectricalSafety.provideThisLater.unoccupied",
                        ),
                    ),
                ),
                arguments(
                    named(
                        "without electrical safety certificate, occupied, and provide later",
                        missingOccupiedProvideLater,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.hasValidCert",
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
                        "without electrical safety certificate and occupied",
                        missingOccupiedNoCert,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.hasValidCert",
                            "commonText.no",
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
                            "propertyDetails.complianceInformation.electricalSafety.hasValidCert",
                            "checkElectricalSafety.provideThisLater.unoccupied",
                        ),
                    ),
                ),
            )

        private fun rowsWithUploads(
            certKeyPrefix: String,
            uploadedFiles: List<UploadedFileUrl>,
            expiryDate: Any?,
            isValidCertificate: Boolean = true,
        ): List<SummaryListRowViewModel> =
            listOfNotNull(
                SummaryListRowViewModel(
                    "propertyDetails.complianceInformation.certificateStatus",
                    TagValue(
                        if (isValidCertificate) {
                            "propertyDetails.complianceInformation.valid"
                        } else {
                            "propertyDetails.complianceInformation.expired"
                        },
                        if (isValidCertificate) "green" else "red",
                    ),
                ),
                SummaryListRowViewModel(
                    "propertyDetails.complianceInformation.electricalSafety.whichCertificate",
                    "$certKeyPrefix.certificate",
                ),
                SummaryListRowViewModel(
                    "propertyDetails.complianceInformation.electricalSafety.yourCertificate",
                    uploadedFiles,
                ),
                expiryDate?.let {
                    SummaryListRowViewModel(
                        "propertyDetails.complianceInformation.expiryDate",
                        it,
                    )
                },
            )

        private fun rowsWithoutUploads(
            certKeyPrefix: String,
            expiryDate: Any? = null,
        ): List<SummaryListRowViewModel> =
            listOfNotNull(
                SummaryListRowViewModel(
                    "propertyDetails.complianceInformation.certificateStatus",
                    TagValue("propertyDetails.complianceInformation.expired", "red"),
                ),
                SummaryListRowViewModel(
                    "propertyDetails.complianceInformation.electricalSafety.whichCertificate",
                    "$certKeyPrefix.certificate",
                ),
                expiryDate?.let {
                    SummaryListRowViewModel(
                        "propertyDetails.complianceInformation.expiryDate",
                        it,
                    )
                },
            )

        @JvmStatic
        private fun provideInsetTextKeys() =
            arrayOf(
                arguments(named("with compliant electrical safety certificate", compliant), null),
                arguments(named("with expired cert (unoccupied, after upload)", expiredAfterUpload), null),
                arguments(
                    named("with expired cert (occupied, after upload)", expiredAfterUploadOccupied),
                    "checkElectricalSafety.occupiedNoCertInsetText",
                ),
                arguments(named("with missing cert (unoccupied)", missingUnoccupied), null),
                arguments(
                    named("with missing cert (occupied, no cert)", missingOccupiedNoCert),
                    "checkElectricalSafety.occupiedNoCertInsetText",
                ),
                arguments(named("with missing cert (occupied, provide later)", missingOccupiedProvideLater), null),
            )
    }
}
