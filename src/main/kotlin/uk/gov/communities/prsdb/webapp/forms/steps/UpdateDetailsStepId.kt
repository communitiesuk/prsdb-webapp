package uk.gov.communities.prsdb.webapp.forms.steps

enum class UpdateDetailsStepId(
    override val urlPathSegment: String,
) : StepId {
    InitialStep("/landlord-details"),
    UpdateEmail("email"),
    ChangeDetailsSession("details"),
}
