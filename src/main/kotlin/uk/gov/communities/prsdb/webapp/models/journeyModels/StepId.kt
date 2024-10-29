package uk.gov.communities.prsdb.webapp.models.journeyModels

interface StepId {
    val urlPathSegment: String
}

enum class LandlordRegistrationStepId(
    override val urlPathSegment: String,
) : StepId {
    Start("start"),
    Second("second"),
    End("end"),
}
