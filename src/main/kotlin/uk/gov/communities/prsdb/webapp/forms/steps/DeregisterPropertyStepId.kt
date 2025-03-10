package uk.gov.communities.prsdb.webapp.forms.steps

enum class DeregisterPropertyStepId(
    override val urlPathSegment: String,
) : StepId {
    AreYouSure("are-you-sure"),
    Reason("reason"),
}
