package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME

class GroupedUpdateJourneyExtensions {
    companion object {
        fun Map<String, Any>.withBackUrlIfNotChangingAnswer(
            backUrl: String?,
            isChangingAnswer: Boolean,
        ) = if (backUrl == null || isChangingAnswer) {
            this
        } else {
            this + (BACK_URL_ATTR_NAME to backUrl)
        }
    }
}
