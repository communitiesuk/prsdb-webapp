package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
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
                        value = MessageKeyConverter.convert(propertyCompliance.hasFireSafetyDeclaration),
                        actionText = "forms.links.view",
                        // TODO PRSD-1314 add Review Fire Safety Info url
                        actionLink = "#",
                        withActionLink = withActionLinks,
                    )
                    addRow(
                        key = "propertyDetails.complianceInformation.landlordResponsibilities.keepPropertySafe",
                        value = MessageKeyConverter.convert(propertyCompliance.hasKeepPropertySafeDeclaration),
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
                        value = MessageKeyConverter.convert(propertyCompliance.hasResponsibilityToTenantsDeclaration),
                        actionText = "forms.links.view",
                        // TODO PRSD-1316 add Review Legal Responsibilities to Tenants Info url
                        actionLink = "#",
                        withActionLink = withActionLinks,
                    )
                }.toList()
    }
}
