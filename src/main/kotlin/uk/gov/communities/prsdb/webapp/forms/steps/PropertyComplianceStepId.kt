package uk.gov.communities.prsdb.webapp.forms.steps

enum class PropertyComplianceStepId(
    override val urlPathSegment: String,
) : StepId {
    GasSafety("gas-safety-certificate"),
    GasSafetyIssueDate("gas-safety-certificate-issue-date"),
    GasSafetyEngineerNum("gas-safety-certificate-engineer-number"),
    GasSafetyUpload("gas-safety-certificate-upload"),
    GasSafetyOutdated("gas-safety-certificate-outdated"),
    GasSafetyExemption("gas-safety-certificate-exemption"),
    GasSafetyExemptionReason("gas-safety-certificate-exemption-reason"),
    GasSafetyExemptionMissing("gas-safety-certificate-exemption-missing"),
    EICR("eicr"),
    EPC("epc"),
    CheckAndSubmit("check-and-submit"),
    Declaration("declaration"),
}
