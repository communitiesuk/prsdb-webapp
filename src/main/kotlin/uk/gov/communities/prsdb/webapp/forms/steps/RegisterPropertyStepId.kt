package uk.gov.communities.prsdb.webapp.forms.steps

enum class RegisterPropertyStepId(
    override val urlPathSegment: String,
) : StepId {
    PlaceholderPage("placeholder"),
    LookupAddress("lookup-address"),
    SelectAddress("select-address"),
    ManualAddress("manual-address"),
    AlreadyRegistered("already-registered"),
    SelectLocalAuthority("select-local-authority"),
    PropertyType("property-type"),
    OwnershipType("ownership-type"),
    Occupancy("occupancy"),
    NumberOfHouseholds("number-of-households"),
    NumberOfPeople("number-of-people"),
}
