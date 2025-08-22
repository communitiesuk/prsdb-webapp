package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder

class LandlordResponsibilitiesViewModelBuilderTests {
    @ParameterizedTest(name = "{1}")
    @MethodSource("provideLandlordResponsibilityRows")
    fun `fromEntity returns the correct summary rows`(
        propertyCompliance: PropertyCompliance,
        withActionLinks: Boolean,
        expectedRows: List<SummaryListRowViewModel>,
    ) {
        val landlordResponsibilitiesRows =
            LandlordResponsibilitiesViewModelBuilder.fromEntity(
                propertyCompliance,
                withActionLinks = withActionLinks,
            )

        assertIterableEquals(landlordResponsibilitiesRows, expectedRows)
    }

    companion object {
        private val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()

        @JvmStatic
        private fun provideLandlordResponsibilityRows() =
            arrayOf(
                arguments(
                    propertyCompliance,
                    named("with action links", true),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.landlordResponsibilities.fireSafety",
                            "propertyDetails.complianceInformation.landlordResponsibilities.readAndConfirmed",
                            SummaryListRowActionViewModel(
                                "forms.links.view",
                                PropertyComplianceController.getReviewPropertyComplianceStepPath(
                                    propertyCompliance.propertyOwnership.id,
                                    PropertyComplianceStepId.FireSafetyDeclaration,
                                ),
                            ),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.landlordResponsibilities.keepPropertySafe",
                            "propertyDetails.complianceInformation.landlordResponsibilities.readAndConfirmed",
                            SummaryListRowActionViewModel(
                                "forms.links.view",
                                PropertyComplianceController.getReviewPropertyComplianceStepPath(
                                    propertyCompliance.propertyOwnership.id,
                                    PropertyComplianceStepId.KeepPropertySafe,
                                ),
                            ),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.landlordResponsibilities.responsibilityToTenants",
                            "propertyDetails.complianceInformation.landlordResponsibilities.readAndConfirmed",
                            SummaryListRowActionViewModel(
                                "forms.links.view",
                                PropertyComplianceController.getReviewPropertyComplianceStepPath(
                                    propertyCompliance.propertyOwnership.id,
                                    PropertyComplianceStepId.ResponsibilityToTenants,
                                ),
                            ),
                        ),
                    ),
                ),
                arguments(
                    propertyCompliance,
                    named("without action links", false),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.landlordResponsibilities.fireSafety",
                            "propertyDetails.complianceInformation.landlordResponsibilities.readAndConfirmed",
                            null,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.landlordResponsibilities.keepPropertySafe",
                            "propertyDetails.complianceInformation.landlordResponsibilities.readAndConfirmed",
                            null,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.landlordResponsibilities.responsibilityToTenants",
                            "propertyDetails.complianceInformation.landlordResponsibilities.readAndConfirmed",
                            null,
                        ),
                    ),
                ),
            )
    }
}
