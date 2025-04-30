package uk.gov.communities.prsdb.webapp.forms.steps

enum class UpdatePropertyDetailsStepId(
    override val urlPathSegment: String,
    override val groupIdentifier: UpdateGroupIdentifier,
) : GroupedStepId<UpdateGroupIdentifier> {
    UpdateOwnershipType("ownership-type", UpdateGroupIdentifier.Ownership),
    UpdateOccupancy("occupancy", UpdateGroupIdentifier.Occupancy),
    UpdateNumberOfHouseholds("number-of-households", UpdateGroupIdentifier.Occupancy),
    UpdateNumberOfPeople("number-of-people", UpdateGroupIdentifier.Occupancy),
    CheckYourOccupancyAnswers("check-occupancy", UpdateGroupIdentifier.Occupancy),
    UpdateLicensingType("licensing-type", UpdateGroupIdentifier.Licensing),
    UpdateSelectiveLicence("selective-licence", UpdateGroupIdentifier.Licensing),
    UpdateHmoMandatoryLicence("hmo-mandatory-licence", UpdateGroupIdentifier.Licensing),
    UpdateHmoAdditionalLicence("hmo-additional-licence", UpdateGroupIdentifier.Licensing),
    CheckYourLicensingAnswers("check-licensing", UpdateGroupIdentifier.Licensing),
    ;

    companion object {
        fun fromPathSegment(segment: String): UpdatePropertyDetailsStepId? = entries.find { it.urlPathSegment == segment }
    }
}

enum class UpdateGroupIdentifier(
    val identifierString: String,
) {
    Ownership("-OWNERSHIP"),
    Occupancy("-OCCUPANCY"),
    Licensing("-LICENSING"),
}
