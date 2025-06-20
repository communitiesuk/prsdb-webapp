package uk.gov.communities.prsdb.webapp.forms.steps

enum class UpdatePropertyDetailsStepId(
    override val urlPathSegment: String,
    override val groupIdentifier: UpdatePropertyDetailsGroupIdentifier,
    override val isCheckYourAnswersStepId: Boolean = false,
) : GroupedUpdateStepId<UpdatePropertyDetailsGroupIdentifier> {
    UpdateOwnershipType("ownership-type", UpdatePropertyDetailsGroupIdentifier.Ownership),
    UpdateLicensingType("licensing-type", UpdatePropertyDetailsGroupIdentifier.Licensing),
    UpdateSelectiveLicence("selective-licence", UpdatePropertyDetailsGroupIdentifier.Licensing),
    UpdateHmoMandatoryLicence("hmo-mandatory-licence", UpdatePropertyDetailsGroupIdentifier.Licensing),
    UpdateHmoAdditionalLicence("hmo-additional-licence", UpdatePropertyDetailsGroupIdentifier.Licensing),
    CheckYourLicensingAnswers("check-licensing-answers", UpdatePropertyDetailsGroupIdentifier.Licensing, true),
    UpdateOccupancy("occupancy", UpdatePropertyDetailsGroupIdentifier.Occupancy),
    UpdateOccupancyNumberOfHouseholds("occupancy-number-of-households", UpdatePropertyDetailsGroupIdentifier.Occupancy),
    UpdateOccupancyNumberOfPeople("occupancy-number-of-people", UpdatePropertyDetailsGroupIdentifier.Occupancy),
    CheckYourOccupancyAnswers("check-occupancy-answers", UpdatePropertyDetailsGroupIdentifier.Occupancy, true),
    UpdateHouseholdsOccupancy("households-occupancy", UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds),
    UpdateNumberOfHouseholds("number-of-households", UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds),
    UpdateHouseholdsNumberOfPeople("households-number-of-people", UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds),
    CheckYourHouseholdsAnswers("check-households-answers", UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds, true),
    UpdatePeopleOccupancy("people-occupancy", UpdatePropertyDetailsGroupIdentifier.NumberOfPeople),
    UpdatePeopleNumberOfHouseholds("people-number-of-households", UpdatePropertyDetailsGroupIdentifier.NumberOfPeople),
    UpdateNumberOfPeople("number-of-people", UpdatePropertyDetailsGroupIdentifier.NumberOfPeople),
    CheckYourPeopleAnswers("check-people-answers", UpdatePropertyDetailsGroupIdentifier.NumberOfPeople, true),
    ;

    companion object {
        fun fromPathSegment(segment: String) = entries.find { it.urlPathSegment == segment }
    }
}

enum class UpdatePropertyDetailsGroupIdentifier {
    Ownership,
    Licensing,
    Occupancy,
    NumberOfHouseholds,
    NumberOfPeople,
    ;

    val relatedGroups =
        if (this in UpdatePropertyDetailsGroupIdentifier.occupancyRelatedGroups) {
            UpdatePropertyDetailsGroupIdentifier.occupancyRelatedGroups
        } else {
            listOf(this)
        }

    companion object {
        private val occupancyRelatedGroups = listOf(Occupancy, NumberOfHouseholds, NumberOfPeople)
    }
}
