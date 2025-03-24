package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.constants.DETAILS_PATH_SEGMENT

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
    UpdateDetails(DETAILS_PATH_SEGMENT),
}
