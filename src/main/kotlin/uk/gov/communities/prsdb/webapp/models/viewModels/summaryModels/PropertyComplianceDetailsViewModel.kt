package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow

class PropertyComplianceDetailsViewModel(
    private val propertyCompliance: PropertyCompliance,
    private val withActionLinks: Boolean = true,
    private val withNotificationMessages: Boolean = true,
) {
    private val changeLinkMessageKey = "forms.links.change"

    private val viewLinkMessageKey = "forms.links.view"

    // TODO PRSD-1297 add update links to notification messages
    var notificationMessages = mutableListOf<String>()

    val gasSafetySummaryList: List<SummaryListRowViewModel> = mutableListOf<SummaryListRowViewModel>()

    val eicrSummaryList: List<SummaryListRowViewModel> = mutableListOf<SummaryListRowViewModel>()

    val epcSummaryList: List<SummaryListRowViewModel> = mutableListOf<SummaryListRowViewModel>()

    val landlordResponsibilitiesSummaryList: List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    key = "propertyDetails.complianceInformation.landlordResponsibilities.fireSafety",
                    value = MessageKeyConverter.convert(propertyCompliance.hasFireSafetyDeclaration),
                    actionText = viewLinkMessageKey,
                    // TODO PRSD-1314 add Review Fire Safety Info url
                    actionLink = "#",
                    withActionLinks = withActionLinks,
                )
                addRow(
                    key = "propertyDetails.complianceInformation.landlordResponsibilities.keepPropertySafe",
                    value = MessageKeyConverter.convert(propertyCompliance.hasKeepPropertySafeDeclaration),
                    actionText = viewLinkMessageKey,
                    // TODO PRSD-1315 add Review Keep Property Safe Info url
                    actionLink = "#",
                    withActionLinks = withActionLinks,
                )
                addRow(
                    key = "propertyDetails.complianceInformation.landlordResponsibilities.responsibilityToTenants",
                    value = MessageKeyConverter.convert(propertyCompliance.hasResponsibilityToTenantsDeclaration),
                    actionText = viewLinkMessageKey,
                    // TODO PRSD-1314 add Review Legal Responsibilities to Tenants Info url
                    actionLink = "#",
                    withActionLinks = withActionLinks,
                )
            }.toList()

    private fun addMessageToNotificationList(message: String) {
        if (withNotificationMessages) {
            notificationMessages.add(message)
        }
    }
}
