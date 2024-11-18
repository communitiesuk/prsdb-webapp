package uk.gov.communities.prsdb.webapp.forms.steps

enum class RegisterLaUserStepId(
    override val urlPathSegment: String,
) : StepId {
    LandingPage("landing-page"),
    Name("name"),
    Email("email"),
    CheckAnswers("check-answers"),
}
