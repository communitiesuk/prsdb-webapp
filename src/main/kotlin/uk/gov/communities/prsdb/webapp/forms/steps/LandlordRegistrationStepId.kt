package uk.gov.communities.prsdb.webapp.forms.steps

enum class LandlordRegistrationStepId(
    override val urlPathSegment: String,
) : StepId {
    Name("name"),
    Email("email"),
    PhoneNumber("phone-number"),
    CountryOfResidence("country-of-residence"),
    InternationalAddress("international-address"),
    CheckAnswers("check-answers"),
}
