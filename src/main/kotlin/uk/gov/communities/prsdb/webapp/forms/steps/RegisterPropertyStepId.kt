package uk.gov.communities.prsdb.webapp.forms.steps

enum class RegisterPropertyStepId(
    override val urlPathSegment: String,
    override val groupIdentifier: RegisterPropertyGroupIdentifier,
) : GroupedStepId<RegisterPropertyGroupIdentifier> {
    PlaceholderPage("placeholder", RegisterPropertyGroupIdentifier.Address),
    LookupAddress("lookup-address", RegisterPropertyGroupIdentifier.Address),
    SelectAddress("select-address", RegisterPropertyGroupIdentifier.Address),
    NoAddressFound("no-address-found", RegisterPropertyGroupIdentifier.Address),
    ManualAddress("manual-address", RegisterPropertyGroupIdentifier.Address),
    AlreadyRegistered("already-registered", RegisterPropertyGroupIdentifier.Address),
    LocalCouncil("local-council", RegisterPropertyGroupIdentifier.Address),
    PropertyType("property-type", RegisterPropertyGroupIdentifier.PropertyType),
    OwnershipType("ownership-type", RegisterPropertyGroupIdentifier.Ownership),
    Occupancy("occupancy", RegisterPropertyGroupIdentifier.Occupancy),
    NumberOfHouseholds("number-of-households", RegisterPropertyGroupIdentifier.Occupancy),
    NumberOfPeople("number-of-people", RegisterPropertyGroupIdentifier.Occupancy),
    NumberOfBedrooms("number-of-bedrooms", RegisterPropertyGroupIdentifier.Occupancy),
    RentIncludesBills("rent-includes-bills", RegisterPropertyGroupIdentifier.Occupancy),
    BillsIncluded("bills-included", RegisterPropertyGroupIdentifier.Occupancy),
    PropertyFurnished("property-furnished", RegisterPropertyGroupIdentifier.Occupancy),
    RentFrequency("rent-frequency", RegisterPropertyGroupIdentifier.Occupancy),
    LicensingType("licensing-type", RegisterPropertyGroupIdentifier.Licensing),
    SelectiveLicence("selective-licence", RegisterPropertyGroupIdentifier.Licensing),
    HmoMandatoryLicence("hmo-mandatory-licence", RegisterPropertyGroupIdentifier.Licensing),
    HmoAdditionalLicence("hmo-additional-licence", RegisterPropertyGroupIdentifier.Licensing),
    CheckAnswers("check-answers", RegisterPropertyGroupIdentifier.CheckYourAnswers),
}

enum class RegisterPropertyGroupIdentifier {
    Address,
    PropertyType,
    Ownership,
    Occupancy,
    Licensing,
    CheckYourAnswers,
    Declaration,
}
