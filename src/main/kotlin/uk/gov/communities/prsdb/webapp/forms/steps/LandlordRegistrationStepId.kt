package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController

enum class LandlordRegistrationStepId(
    override val urlPathSegment: String,
) : StepId {
    VerifyIdentity(RegisterLandlordController.IDENTITY_VERIFICATION_PATH_SEGMENT),
    Name("name"),
    DateOfBirth("date-of-birth"),
    ConfirmIdentity("confirm-identity"),
    Email("email"),
    PhoneNumber("phone-number"),
    CountryOfResidence("country-of-residence"),
    LookupAddress("lookup-address"),
    SelectAddress("select-address"),
    ManualAddress("manual-address"),
    InternationalAddress("international-address"),
    LookupContactAddress("lookup-contact-address"),
    SelectContactAddress("select-contact-address"),
    ManualContactAddress("manual-contact-address"),
    CheckAnswers("check-answers"),
    Declaration("declaration"),
}
