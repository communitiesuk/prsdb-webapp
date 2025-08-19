package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.constants.PRIVACY_NOTICE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController

enum class LandlordRegistrationStepId(
    override val urlPathSegment: String,
    override val groupIdentifier: LandlordRegistrationGroupIdentifier,
) : GroupedStepId<LandlordRegistrationGroupIdentifier> {
    PrivacyNotice(PRIVACY_NOTICE_PATH_SEGMENT, LandlordRegistrationGroupIdentifier.PrivacyNotice),
    VerifyIdentity(RegisterLandlordController.IDENTITY_VERIFICATION_PATH_SEGMENT, LandlordRegistrationGroupIdentifier.IdentityVerification),
    IdentityNotVerified("identity-not-verified", LandlordRegistrationGroupIdentifier.IdentityVerification),
    Name("name", LandlordRegistrationGroupIdentifier.Name),
    DateOfBirth("date-of-birth", LandlordRegistrationGroupIdentifier.DateOfBirth),
    ConfirmIdentity("confirm-identity", LandlordRegistrationGroupIdentifier.IdentityVerification),
    Email("email", LandlordRegistrationGroupIdentifier.Email),
    PhoneNumber("phone-number", LandlordRegistrationGroupIdentifier.PhoneNumber),
    CountryOfResidence("country-of-residence", LandlordRegistrationGroupIdentifier.Address),
    LookupAddress("lookup-address", LandlordRegistrationGroupIdentifier.Address),
    NoAddressFound("no-address-found", LandlordRegistrationGroupIdentifier.Address),
    SelectAddress("select-address", LandlordRegistrationGroupIdentifier.Address),
    ManualAddress("manual-address", LandlordRegistrationGroupIdentifier.Address),
    NonEnglandOrWalesAddress("neither-england-nor-wales-address", LandlordRegistrationGroupIdentifier.Address),
    LookupContactAddress("lookup-contact-address", LandlordRegistrationGroupIdentifier.Address),
    NoContactAddressFound("no-contact-address-found", LandlordRegistrationGroupIdentifier.Address),
    SelectContactAddress("select-contact-address", LandlordRegistrationGroupIdentifier.Address),
    ManualContactAddress("manual-contact-address", LandlordRegistrationGroupIdentifier.Address),
    CheckAnswers("check-answers", LandlordRegistrationGroupIdentifier.CheckYourAnswers),
    Declaration("declaration", LandlordRegistrationGroupIdentifier.Declaration),
}

enum class LandlordRegistrationGroupIdentifier {
    PrivacyNotice,
    IdentityVerification,
    Name,
    DateOfBirth,
    Email,
    PhoneNumber,
    Address,
    CheckYourAnswers,
    Declaration,
}
