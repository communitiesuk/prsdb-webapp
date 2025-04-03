package uk.gov.communities.prsdb.webapp.forms.steps

enum class PropertyComplianceStepId(
    override val urlPathSegment: String,
) : StepId {
    GasSafety("gas-safety-certificate"),
    GasSafetyIssueDate("gas-safety-certificate-issue-date"),
    GasSafetyExemption("gas-safety-certificate-exemption"),
    EICR("eicr"),
    EPC("epc"),
    CheckAndSubmit("check-and-submit"),
    Declaration("declaration"),
}
