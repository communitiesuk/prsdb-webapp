package uk.gov.communities.prsdb.webapp.forms.steps

enum class RegisterLaUserStepId(
    override val urlPathSegment: String,
) : StepId {
    Name("name"),
    Email("email"),
    CheckAnswers("check-answers"),
}
