package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.constants.TAG_COLOUR_GREEN
import uk.gov.communities.prsdb.webapp.constants.TAG_COLOUR_RED

data class TagValue(
    val messageKey: String,
    val colour: String,
) {
    companion object {
        val VALID = TagValue("propertyDetails.complianceInformation.valid", TAG_COLOUR_GREEN)
        val EXPIRED = TagValue("propertyDetails.complianceInformation.expired", TAG_COLOUR_RED)
    }
}
