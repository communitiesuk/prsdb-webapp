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
    @ParameterizedTest(name = "{0} and {1}")
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
        private val propertyComplianceWithFireSafety =
            PropertyComplianceBuilder.createWithInDateCertsAndSetFireSafetyDeclaration(true)

        @JvmStatic
        private fun provideLandlordResponsibilityRows() =
            arrayOf(
                arguments(
                    named(
                        "with fire safety declaration",
                        PropertyComplianceBuilder.createWithInDateCertsAndSetFireSafetyDeclaration(true),
                    ),
                    named("with action links", true),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.landlordResponsibilities.fireSafety",
                            "commonText.yes",
                            // TODO PRSD-1314 add Review Fire Safety Info url
                            SummaryListRowActionViewModel("forms.links.view", "#"),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.landlordResponsibilities.keepPropertySafe",
                            "commonText.yes",
                            SummaryListRowActionViewModel(
                                "forms.links.view",
                                PropertyComplianceController.getReviewPropertyComplianceStepPath(
                                    propertyComplianceWithFireSafety.propertyOwnership.id,
                                    PropertyComplianceStepId.KeepPropertySafe,
                                ),
                            ),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.landlordResponsibilities.responsibilityToTenants",
                            "commonText.yes",
                            // TODO PRSD-1316 add Review Legal Responsibilities to Tenants Info url
                            SummaryListRowActionViewModel("forms.links.view", "#"),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "without fire safety declaration",
                        PropertyComplianceBuilder.createWithInDateCertsAndSetFireSafetyDeclaration(false),
                    ),
                    named("without action links", false),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.landlordResponsibilities.fireSafety",
                            "commonText.no",
                            null,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.landlordResponsibilities.keepPropertySafe",
                            "commonText.yes",
                            null,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.landlordResponsibilities.responsibilityToTenants",
                            "commonText.yes",
                            null,
                        ),
                    ),
                ),
            )
    }
}
