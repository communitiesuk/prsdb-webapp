package uk.gov.communities.prsdb.webapp.forms.steps

enum class UpdatePropertyDetailsStepId(
    override val urlPathSegment: String,
) : StepId {
    UpdateOwnershipType("ownership-type"),
    UpdateOccupancy("occupancy"),
    UpdateNumberOfHouseholds("number-of-households"),
    UpdateNumberOfPeople("number-of-people"),
    UpdateLicensingType("licensing-type"),
    UpdateSelectiveLicence("selective-licence"),
    UpdateHmoMandatoryLicence("hmo-mandatory-licence"),
    UpdateHmoAdditionalLicence("hmo-additional-licence"),
    CheckYourLicensing("check-licensing"),
    CheckYourOccupancy("check-occupancy"),
}
