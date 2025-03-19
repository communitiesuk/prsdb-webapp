package uk.gov.communities.prsdb.webapp.forms.steps

enum class DeregisterLandlordStepId(
    override val urlPathSegment: String,
) : StepId {
    AreYouSure("are-you-sure"),
}
