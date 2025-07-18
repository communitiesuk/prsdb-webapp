package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder

class GasSafetyViewModelBuilderTests {
    @ParameterizedTest(name = "{0} and {1}")
    @MethodSource("provideGasSafetyRows")
    fun `fromEntity returns the correct summary rows`(
        propertyCompliance: PropertyCompliance,
        withActionLinks: Boolean,
        expectedRows: List<SummaryListRowViewModel>,
    ) {
        val gasSafetyRows =
            GasSafetyViewModelBuilder.fromEntity(
                propertyCompliance,
                withActionLinks = withActionLinks,
            )

        assertIterableEquals(expectedRows, gasSafetyRows)
    }

    companion object {
        private val compliant = PropertyComplianceBuilder.createWithInDateCerts()
        private val expiredAfterUpload = PropertyComplianceBuilder.createWithGasCertExpiredAfterUpload()
        private val expiredBeforeUpload = PropertyComplianceBuilder.createWithGasCertExpiredBeforeUpload()
        private val exempt = PropertyComplianceBuilder.createWithCertExemptions(gasExemption = GasSafetyExemptionReason.NO_GAS_SUPPLY)
        private val missing = PropertyComplianceBuilder.createWithMissingCerts()

        @JvmStatic
        private fun provideGasSafetyRows() =
            arrayOf(
                arguments(
                    named(
                        "with compliant gas safety certificate",
                        compliant,
                    ),
                    named("with action links", true),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafetyCertificate",
                            "propertyDetails.complianceInformation.gasSafety.downloadCertificate",
                            SummaryListRowActionViewModel(
                                "forms.links.change",
                                "/landlord/provide-compliance-certificates/" +
                                    "${compliant.propertyOwnership.id}/update/update-gas-safety-certificate",
                            ),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            compliant.gasSafetyCertIssueDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.validUntil",
                            compliant.gasSafetyCertExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafeEngineerNumber",
                            compliant.gasSafetyCertEngineerNum,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with expired after upload gas safety certificate",
                        expiredAfterUpload,
                    ),
                    named("without action links", false),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafetyCertificate",
                            "propertyDetails.complianceInformation.gasSafety.downloadExpiredCertificate",
                            null,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            expiredAfterUpload.gasSafetyCertIssueDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.validUntil",
                            expiredAfterUpload.gasSafetyCertExpiryDate,
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
                    named("without action links", true),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafetyCertificate",
                            "propertyDetails.complianceInformation.expired",
                            SummaryListRowActionViewModel(
                                "forms.links.change",
                                "/landlord/provide-compliance-certificates/" +
                                    "${expiredBeforeUpload.propertyOwnership.id}/update/update-gas-safety-certificate",
                            ),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            expiredBeforeUpload.gasSafetyCertIssueDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.validUntil",
                            expiredBeforeUpload.gasSafetyCertExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafeEngineerNumber",
                            expiredBeforeUpload.gasSafetyCertEngineerNum,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with gas safety exemption",
                        exempt,
                    ),
                    named("without action links", false),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafetyCertificate",
                            "propertyDetails.complianceInformation.exempt",
                            null,
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
                    named("without action links", true),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.gasSafety.gasSafetyCertificate",
                            "propertyDetails.complianceInformation.notAdded",
                            SummaryListRowActionViewModel(
                                "forms.links.change",
                                "/landlord/provide-compliance-certificates/" +
                                    "${missing.propertyOwnership.id}/update/update-gas-safety-certificate",
                            ),
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
