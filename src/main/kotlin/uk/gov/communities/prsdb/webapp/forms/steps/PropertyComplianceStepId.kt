package uk.gov.communities.prsdb.webapp.forms.steps

enum class PropertyComplianceStepId(
    override val urlPathSegment: String,
    override val groupIdentifier: PropertyComplianceGroupIdentifier,
    override val isCheckYourAnswersStepId: Boolean = false,
) : GroupedUpdateStepId<PropertyComplianceGroupIdentifier> {
    GasSafety("gas-safety-certificate", PropertyComplianceGroupIdentifier.GasSafety),
    UpdateGasSafety("update-gas-safety-certificate", PropertyComplianceGroupIdentifier.GasSafety),
    GasSafetyEngineerNum("gas-safety-certificate-engineer-number", PropertyComplianceGroupIdentifier.GasSafety),
    UpdateEICR("update-eicr", PropertyComplianceGroupIdentifier.Eicr),
    UpdateEpc("update-epc", PropertyComplianceGroupIdentifier.Epc),
    CheckAutoMatchedEpc("check-auto-matched-epc", PropertyComplianceGroupIdentifier.Epc),
    EpcLookup("epc-lookup", PropertyComplianceGroupIdentifier.Epc),
    UpdateMeesCheckAutoMatchedEpc("update-mees-check-auto-matched-epc", PropertyComplianceGroupIdentifier.Mees),
    UpdateMeesCheckMatchedEpc("update-mees-check-matched-epc", PropertyComplianceGroupIdentifier.Mees),
    UpdateMeesEpcLookup("update-mees-epc-lookup", PropertyComplianceGroupIdentifier.Mees),
    UpdateMeesEpcNotFound("update-mees-epc-not-found", PropertyComplianceGroupIdentifier.Mees),
    UpdateMeesEpcExpiryCheck("update-mees-epc-expiry-check", PropertyComplianceGroupIdentifier.Mees),
    UpdateMeesEpcExpired("update-mees-epc-expired", PropertyComplianceGroupIdentifier.Mees),
    UpdateMeesEpcExemptionReason("update-mees-epc-exemption-reason", PropertyComplianceGroupIdentifier.Mees),
    UpdateMeesEpcExemptionConfirmation("update-mees-epc-exemption-confirmation", PropertyComplianceGroupIdentifier.Mees),
    UpdateMeesMeesExemptionCheck("update-mees-mees-exemption-check", PropertyComplianceGroupIdentifier.Mees),
    UpdateMeesMeesExemptionReason("update-mees-mees-exemption-reason", PropertyComplianceGroupIdentifier.Mees),
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
