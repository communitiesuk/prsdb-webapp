package uk.gov.communities.prsdb.webapp.forms.steps

enum class ProvideComplianceStepId(
    override val urlPathSegment: String,
) : StepId {
    InitialPlaceholder("initial-placeholder"),
    NonInitialPlaceholder("non-initial-placeholder"),
}
