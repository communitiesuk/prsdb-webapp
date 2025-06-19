package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.constants.LANDING_PAGE_PATH_SEGMENT

enum class RegisterLaUserStepId(
    override val urlPathSegment: String,
) : StepId {
    LandingPage(LANDING_PAGE_PATH_SEGMENT),
    Name("name"),
    Email("email"),
    CheckAnswers("check-answers"),
}
