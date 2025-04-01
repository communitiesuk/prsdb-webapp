package uk.gov.communities.prsdb.webapp.forms.steps

enum class ProvideComplianceStepId(
    override val urlPathSegment: String,
) : StepId {
    GasSafety("gas-safety"),
    EICR("eicr"),
    EPC("epc"),
    CheckAndSubmit("check-and-submit"),
    Declaration("declaration"),
}
