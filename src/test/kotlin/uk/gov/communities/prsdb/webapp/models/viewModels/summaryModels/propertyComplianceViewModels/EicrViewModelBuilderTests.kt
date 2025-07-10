package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder

class EicrViewModelBuilderTests {
    @ParameterizedTest(name = "{0} and {1}")
    @MethodSource("provideSEicrRows")
    fun `fromEntity returns the correct summary rows`(
        propertyCompliance: PropertyCompliance,
        withActionLinks: Boolean,
        expectedRows: List<SummaryListRowViewModel>,
    ) {
        val eicrRows =
            EicrViewModelBuilder.fromEntity(
                propertyCompliance,
                withActionLinks = withActionLinks,
            )

        assertIterableEquals(eicrRows, expectedRows)
    }

    companion object {
        private val compliant = PropertyComplianceBuilder.createWithInDateCerts()
        private val expiredAfterUpload = PropertyComplianceBuilder.createWithEicrExpiredAfterUpload()
        private val expiredBeforeUpload = PropertyComplianceBuilder.createWithEicrExpiredBeforeUpload()
        private val exempt = PropertyComplianceBuilder.createWithCertExemptions(eicrExemption = EicrExemptionReason.LIVE_IN_LANDLORD)
        private val missing = PropertyComplianceBuilder.createWithMissingCerts()

        @JvmStatic
        private fun provideSEicrRows() =
            arrayOf(
                arguments(
                    named(
                        "with compliant eicr",
                        compliant,
                    ),
                    named("with action links", true),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.eicr",
                            "propertyDetails.complianceInformation.electricalSafety.downloadEicr",
                            // TODO PRSD-1246 add Update EICR Compliance Link
                            SummaryListRowActionViewModel("forms.links.change", "#"),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            compliant.eicrIssueDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.validUntil",
                            compliant.eicrExpiryDate,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with expired after upload eicr",
                        expiredAfterUpload,
                    ),
                    named("without action links", false),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.eicr",
                            "propertyDetails.complianceInformation.electricalSafety.downloadExpiredEicr",
                            null,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            expiredAfterUpload.eicrIssueDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.validUntil",
                            expiredAfterUpload.eicrExpiryDate,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with expired before upload eicr",
                        expiredBeforeUpload,
                    ),
                    named("without action links", true),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.eicr",
                            "propertyDetails.complianceInformation.expired",
                            // TODO PRSD-1246 add Update EICR Compliance Link
                            SummaryListRowActionViewModel("forms.links.change", "#"),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.issueDate",
                            expiredBeforeUpload.eicrIssueDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.validUntil",
                            expiredBeforeUpload.eicrExpiryDate,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with eicr exemption",
                        exempt,
                    ),
                    named("without action links", false),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.eicr",
                            "propertyDetails.complianceInformation.exempt",
                            null,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.exemption",
                            MessageKeyConverter.convert(EicrExemptionReason.LIVE_IN_LANDLORD),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "without eicr",
                        missing,
                    ),
                    named("without action links", true),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.electricalSafety.eicr",
                            "propertyDetails.complianceInformation.notAdded",
                            // TODO PRSD-1246 add Update EICR Compliance Link
                            SummaryListRowActionViewModel("forms.links.change", "#"),
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
