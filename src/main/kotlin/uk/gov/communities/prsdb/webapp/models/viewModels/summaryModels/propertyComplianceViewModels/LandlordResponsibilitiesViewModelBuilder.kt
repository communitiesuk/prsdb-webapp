package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.FireSafetyDeclarationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.KeepPropertySafeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.ResponsibilityToTenantsStep
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class LandlordResponsibilitiesViewModelBuilder {
    companion object {
        fun fromEntity(
            propertyCompliance: PropertyCompliance,
            withActionLinks: Boolean,
        ): List<SummaryListRowViewModel> =
            mutableListOf<SummaryListRowViewModel>()
                .apply {
                    addRow(
                        key = "propertyDetails.complianceInformation.landlordResponsibilities.fireSafety",
                        value = "propertyDetails.complianceInformation.landlordResponsibilities.readAndConfirmed",
                        actionText = "forms.links.view",
                        actionLink =
                            PropertyComplianceController.getReviewPropertyComplianceStepPath(
                                propertyCompliance.propertyOwnership.id,
                                FireSafetyDeclarationStep.ROUTE_SEGMENT,
                            ),
                        withActionLink = withActionLinks,
                    )
                    addRow(
                        key = "propertyDetails.complianceInformation.landlordResponsibilities.keepPropertySafe",
                        value = "propertyDetails.complianceInformation.landlordResponsibilities.readAndConfirmed",
                        actionText = "forms.links.view",
                        actionLink =
                            PropertyComplianceController.getReviewPropertyComplianceStepPath(
                                propertyCompliance.propertyOwnership.id,
                                KeepPropertySafeStep.ROUTE_SEGMENT,
                            ),
                        withActionLink = withActionLinks,
                    )
                    addRow(
                        key = "propertyDetails.complianceInformation.landlordResponsibilities.responsibilityToTenants",
                        value = "propertyDetails.complianceInformation.landlordResponsibilities.readAndConfirmed",
                        actionText = "forms.links.view",
                        actionLink =
                            PropertyComplianceController.getReviewPropertyComplianceStepPath(
                                propertyCompliance.propertyOwnership.id,
                                ResponsibilityToTenantsStep.ROUTE_SEGMENT,
                            ),
                        withActionLink = withActionLinks,
                    )
                }.toList()
    }
}
