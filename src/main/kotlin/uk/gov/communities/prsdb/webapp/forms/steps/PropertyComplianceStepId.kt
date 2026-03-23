package uk.gov.communities.prsdb.webapp.forms.steps

enum class PropertyComplianceStepId(
    override val urlPathSegment: String,
    override val groupIdentifier: PropertyComplianceGroupIdentifier,
    override val isCheckYourAnswersStepId: Boolean = false,
) : GroupedUpdateStepId<PropertyComplianceGroupIdentifier> {
    UpdateMeesCheckAutoMatchedEpc("update-mees-check-auto-matched-epc", PropertyComplianceGroupIdentifier.Mees),
    UpdateMeesCheckMatchedEpc("update-mees-check-matched-epc", PropertyComplianceGroupIdentifier.Mees),
    UpdateMeesEpcNotFound("update-mees-epc-not-found", PropertyComplianceGroupIdentifier.Mees),
    UpdateMeesEpcExpiryCheck("update-mees-epc-expiry-check", PropertyComplianceGroupIdentifier.Mees),
    UpdateMeesEpcExpired("update-mees-epc-expired", PropertyComplianceGroupIdentifier.Mees),
    UpdateMeesEpcExemptionConfirmation("update-mees-epc-exemption-confirmation", PropertyComplianceGroupIdentifier.Mees),
    UpdateMeesMeesExemptionConfirmation("update-mees-mees-exemption-confirmation", PropertyComplianceGroupIdentifier.Mees),
    UpdateMeesLowEnergyRating("update-mees-low-energy-rating", PropertyComplianceGroupIdentifier.Mees),
    CheckAndSubmit("check-and-submit", PropertyComplianceGroupIdentifier.CheckAndSubmit),
}

enum class PropertyComplianceGroupIdentifier {
    GasSafety,
    Eicr,
    Epc,
    Mees,
    CheckAndSubmit,
}
