package uk.gov.communities.prsdb.webapp.forms.steps

enum class UpdateLandlordDetailsStepId(
    override val urlPathSegment: String,
) : StepId {
    UpdateEmail("email"),
    UpdateName("name"),
    UpdateDateOfBirth("date-of-birth"),
    UpdatePhoneNumber("phone-number"),
    LookupEnglandAndWalesAddress("lookup-address"),
    NoAddressFound("no-address-found"),
    SelectEnglandAndWalesAddress("select-address"),
    ManualEnglandAndWalesAddress("manual-address"),
}
