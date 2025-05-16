package uk.gov.communities.prsdb.webapp.forms.steps

enum class LandlordDetailsUpdateStepId(
    override val urlPathSegment: String,
    override val groupIdentifier: UpdateLandlordDetailsStepGroupIdentifier,
    override val isCheckYourAnswersStepId: Boolean = false,
) : UpdateStepId<UpdateLandlordDetailsStepGroupIdentifier> {
    UpdateEmail("email", UpdateLandlordDetailsStepGroupIdentifier.Email),
    UpdateName("name", UpdateLandlordDetailsStepGroupIdentifier.Name),
    UpdateDateOfBirth("date-of-birth", UpdateLandlordDetailsStepGroupIdentifier.DateOfBirth),
    UpdatePhoneNumber("phone-number", UpdateLandlordDetailsStepGroupIdentifier.PhoneNumber),
    LookupEnglandAndWalesAddress("lookup-address", UpdateLandlordDetailsStepGroupIdentifier.Address),
    NoAddressFound("no-address-found", UpdateLandlordDetailsStepGroupIdentifier.Address),
    SelectEnglandAndWalesAddress("select-address", UpdateLandlordDetailsStepGroupIdentifier.Address),
    ManualEnglandAndWalesAddress("manual-address", UpdateLandlordDetailsStepGroupIdentifier.Address),
    ;

    companion object {
        fun fromPathSegment(segment: String) = LandlordDetailsUpdateStepId.entries.find { it.urlPathSegment == segment }
    }
}

enum class UpdateLandlordDetailsStepGroupIdentifier {
    Email,
    Name,
    DateOfBirth,
    PhoneNumber,
    Address,
}
