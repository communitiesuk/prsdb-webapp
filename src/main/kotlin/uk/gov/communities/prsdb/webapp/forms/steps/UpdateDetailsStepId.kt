package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.constants.DETAILS_PATH_SEGMENT

enum class UpdateDetailsStepId(
    override val urlPathSegment: String,
) : StepId {
    UpdateEmail("email"),
    UpdateName("name"),
    UpdateDetails(DETAILS_PATH_SEGMENT),
}
