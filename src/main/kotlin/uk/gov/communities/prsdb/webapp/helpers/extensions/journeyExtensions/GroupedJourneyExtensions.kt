package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME

class GroupedJourneyExtensions {
    companion object {
        fun Map<String, Any>.withBackUrlIfNotNullAndNotCheckingAnswers(
            backUrl: String?,
            isCheckingAnswers: Boolean,
        ) = if (backUrl == null || isCheckingAnswers) {
            this
        } else {
            this + (BACK_URL_ATTR_NAME to backUrl)
        }
    }
}
