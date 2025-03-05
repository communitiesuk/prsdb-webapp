package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.constants.DETAILS_PATH_SEGMENT

enum class UpdatePropertyDetailsStepId(
    override val urlPathSegment: String,
) : StepId {
    UpdateOwnershipType("ownership-type"),
    UpdateNumberOfPeople("number-of-people"),
    UpdateDetails(DETAILS_PATH_SEGMENT),
}
