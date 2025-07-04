package uk.gov.communities.prsdb.webapp.forms.steps

enum class PropertyComplianceUpdateStepId(
    override val urlPathSegment: String,
    override val groupIdentifier: PropertyComplianceUpdateGroupIdentifier,
    override val isCheckYourAnswersStepId: Boolean = false,
) : GroupedUpdateStepId<PropertyComplianceUpdateGroupIdentifier> {
    UpdateGasSafety("update-gas-safety-certificate", PropertyComplianceUpdateGroupIdentifier.GasSafety),
}

enum class PropertyComplianceUpdateGroupIdentifier {
    GasSafety,
}
