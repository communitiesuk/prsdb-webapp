package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController

enum class RegisterLaUserStepId(
    override val urlPathSegment: String,
) : StepId {
    LandingPage(RegisterLAUserController.LANDING_PAGE_PATH_SEGMENT),
    Name("name"),
    Email("email"),
    CheckAnswers("check-answers"),
}
