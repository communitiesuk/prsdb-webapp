package uk.gov.communities.prsdb.webapp.helpers.extensions

import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels.PropertyComplianceViewModel

fun MutableList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>.addRow(
    mainText: String,
    linkText: String,
    withLinkText: Boolean,
) {
    val linkTextOrNull = if (withLinkText) linkText else null
    add(PropertyComplianceViewModel.PropertyComplianceNotificationMessage(mainText, linkTextOrNull))
}
