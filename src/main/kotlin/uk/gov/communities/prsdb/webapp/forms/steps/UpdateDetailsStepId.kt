package uk.gov.communities.prsdb.webapp.forms.steps

enum class UpdateDetailsStepId(
    override val urlPathSegment: String,
) : StepId {
    UpdateEmail("email"),
    ChangeDetailsSession("details"),
}
