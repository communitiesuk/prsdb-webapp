package uk.gov.communities.prsdb.webapp.forms.steps

enum class RegisterLandlordStepId(
    override val urlPathSegment: String,
) : StepId {
    Email("email"),
    PhoneNumber("phone-number"),
}
