package uk.gov.communities.prsdb.webapp.helpers.extensions

import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels.PropertyComplianceViewModel.PropertyComplianceLinkMessage
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels.PropertyComplianceViewModel.PropertyComplianceNotificationMessage

fun MutableList<PropertyComplianceNotificationMessage>.addRow(
    mainText: String,
    linkUrl: String,
    linkText: String,
    afterLinkText: String,
    beforeLinkText: String? = null,
    withLinkMessage: Boolean,
) {
    val linkMessageOrNull = getLinkMessageOrNull(withLinkMessage, linkUrl, linkText, afterLinkText, beforeLinkText)
    add(PropertyComplianceNotificationMessage(mainText, linkMessageOrNull))
}

fun getLinkMessageOrNull(
    withLinkMessage: Boolean,
    linkUrl: String,
    linkText: String,
    afterLinkText: String,
    beforeLinkText: String?,
): PropertyComplianceLinkMessage? =
    if (withLinkMessage) {
        PropertyComplianceLinkMessage(
            linkUrl = linkUrl,
            linkText = linkText,
            afterLinkText = afterLinkText,
            beforeLinkText = beforeLinkText,
        )
    } else {
        null
    }
