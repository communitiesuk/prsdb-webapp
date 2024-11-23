package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController

enum class LandlordRegistrationStepId(
    override val urlPathSegment: String,
) : StepId {
    VerifyIdentity(RegisterLandlordController.IDENTITY_VERIFICATION_PATH_SEGMENT),
    Name("name"),
    ConfirmIdentity("confirm-identity"),
    Email("email"),
    PhoneNumber("phone-number"),
    CountryOfResidence("country-of-residence"),
    InternationalAddress("international-address"),
    CheckAnswers("check-answers"),
}
