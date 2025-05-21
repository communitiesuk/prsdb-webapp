package uk.gov.communities.prsdb.webapp.forms.steps

enum class UpdatePropertyDetailsStepId(
    override val urlPathSegment: String,
    override val groupIdentifier: UpdatePropertyDetailsGroupIdentifier,
    override val isCheckYourAnswersStepId: Boolean = false,
) : GroupedUpdateStepId<UpdatePropertyDetailsGroupIdentifier> {
    UpdateOwnershipType("ownership-type", UpdatePropertyDetailsGroupIdentifier.Ownership),
    UpdateOccupancy("occupancy", UpdatePropertyDetailsGroupIdentifier.Occupancy),
    UpdateNumberOfHouseholds("number-of-households", UpdatePropertyDetailsGroupIdentifier.Occupancy),
    UpdateNumberOfPeople("number-of-people", UpdatePropertyDetailsGroupIdentifier.Occupancy),
    CheckYourOccupancyAnswers("check-occupancy-answers", UpdatePropertyDetailsGroupIdentifier.Occupancy, true),
    UpdateLicensingType("licensing-type", UpdatePropertyDetailsGroupIdentifier.Licensing),
    UpdateSelectiveLicence("selective-licence", UpdatePropertyDetailsGroupIdentifier.Licensing),
    UpdateHmoMandatoryLicence("hmo-mandatory-licence", UpdatePropertyDetailsGroupIdentifier.Licensing),
    UpdateHmoAdditionalLicence("hmo-additional-licence", UpdatePropertyDetailsGroupIdentifier.Licensing),
    CheckYourLicensingAnswers("check-licensing-answers", UpdatePropertyDetailsGroupIdentifier.Licensing, true),
    ;

    companion object {
        fun fromPathSegment(segment: String) = entries.find { it.urlPathSegment == segment }
    }
}

enum class UpdatePropertyDetailsGroupIdentifier {
    Ownership,
    Occupancy,
    Licensing,
}
