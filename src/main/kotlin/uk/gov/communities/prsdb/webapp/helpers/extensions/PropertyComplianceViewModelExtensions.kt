package uk.gov.communities.prsdb.webapp.helpers.extensions

import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels.PropertyComplianceViewModel.PropertyComplianceLinkMessage
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels.PropertyComplianceViewModel.PropertyComplianceNotificationMessage

fun MutableList<PropertyComplianceNotificationMessage>.addRow(
    mainText: String,
    linkText: String,
    afterLinkText: String,
    beforeLinkText: String? = null,
    withLinkMessage: Boolean,
) {
    val linkMessageOrNull = getLinkMessageOrNull(withLinkMessage, linkText, afterLinkText, beforeLinkText)
    add(PropertyComplianceNotificationMessage(mainText, linkMessageOrNull))
}

fun getLinkMessageOrNull(
    withLinkMessage: Boolean,
    linkText: String,
    afterLinkText: String,
    beforeLinkText: String?,
): PropertyComplianceLinkMessage? =
    if (withLinkMessage) {
        PropertyComplianceLinkMessage(
            linkText = linkText,
            afterLinkText = afterLinkText,
            beforeLinkText = beforeLinkText,
        )
    } else {
        null
    }
