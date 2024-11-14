package uk.gov.communities.prsdb.webapp.forms.steps

enum class LandlordRegistrationStepId(
    override val urlPathSegment: String,
) : StepId {
    Email("email"),
    PhoneNumber("phone-number"),
    Name("name"),
}
