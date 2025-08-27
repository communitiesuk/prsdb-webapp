package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
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
                                PropertyComplianceStepId.FireSafetyDeclaration,
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
                                PropertyComplianceStepId.KeepPropertySafe,
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
                                PropertyComplianceStepId.ResponsibilityToTenants,
                            ),
                        withActionLink = withActionLinks,
                    )
                }.toList()
    }
}
