package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder

class EpcViewModelBuilderTests {
    @ParameterizedTest(name = "{0} and {1}")
    @MethodSource("provideEpcRows")
    fun `fromEntity returns the correct summary rows`(
        propertyCompliance: PropertyCompliance,
        withActionLinks: Boolean,
        expectedRows: List<SummaryListRowViewModel>,
    ) {
        val epcRows =
            EpcViewModelBuilder.fromEntity(
                propertyCompliance,
                withActionLinks = withActionLinks,
            )

        assertIterableEquals(epcRows, expectedRows)
    }

    companion object {
        private val compliant = PropertyComplianceBuilder.createWithInDateCerts()
        private val expired = PropertyComplianceBuilder.createWithOnlyEpcExpiredCert()
        private val exempt = PropertyComplianceBuilder.createWithCertExemptions(epcExemption = EpcExemptionReason.DUE_FOR_DEMOLITION)
        private val missing = PropertyComplianceBuilder.createWithMissingCerts()
        private val meesCompliant = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRatingAndMeesExemptionReason()
        private val meesMissingExemptionReason = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating()

        @JvmStatic
        private fun provideEpcRows() =
            arrayOf(
                arguments(
                    named(
                        "with compliant epc",
                        compliant,
                    ),
                    named("with action links", true),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.epc",
                            "propertyDetails.complianceInformation.energyPerformance.viewEpcLinkText",
                            // TODO PDJB-766: readd change link
                            valueUrl = compliant.epcUrl,
                            valueUrlOpensNewTab = true,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            compliant.epcExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            compliant.epcEnergyRating?.uppercase(),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with expired epc",
                        expired,
                    ),
                    named("without action links", false),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.epc",
                            "propertyDetails.complianceInformation.energyPerformance.viewExpiredEpcLinkText",
                            actions = emptyList(),
                            valueUrl = expired.epcUrl,
                            valueUrlOpensNewTab = true,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            expired.epcExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            expired.epcEnergyRating?.uppercase(),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.didTenancyStartBeforeEpcExpired",
                            "commonText.no",
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with epc exemption",
                        exempt,
                    ),
                    named("without action links", false),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.epc",
                            "propertyDetails.complianceInformation.notRequired",
                            emptyList(),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.exemption",
                            MessageKeyConverter.convert(EpcExemptionReason.DUE_FOR_DEMOLITION),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "without epc",
                        missing,
                    ),
                    named("without action links", true),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.epc",
                            "propertyDetails.complianceInformation.notAdded",
                            // TODO PDJB-766: readd change link
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.exemption",
                            "propertyDetails.complianceInformation.noExemption",
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with low rating epc and mees exemption",
                        meesCompliant,
                    ),
                    named("with action links", true),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.epc",
                            "propertyDetails.complianceInformation.energyPerformance.viewEpcLinkText",
                            // TODO PDJB-766: readd change link
                            valueUrl = meesCompliant.epcUrl,
                            valueUrlOpensNewTab = true,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            meesCompliant.epcExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            meesCompliant.epcEnergyRating?.uppercase(),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.meesExemption",
                            MessageKeyConverter.convert(MeesExemptionReason.PROPERTY_DEVALUATION),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with low rating epc and without mees exemption",
                        meesMissingExemptionReason,
                    ),
                    named("with action links", false),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.epc",
                            "propertyDetails.complianceInformation.energyPerformance.viewEpcLinkText",
                            actions = emptyList(),
                            valueUrl = meesMissingExemptionReason.epcUrl,
                            valueUrlOpensNewTab = true,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            meesMissingExemptionReason.epcExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            meesMissingExemptionReason.epcEnergyRating?.uppercase(),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.meesExemption",
                            "commonText.none",
                        ),
                    ),
                ),
            )
    }
}
