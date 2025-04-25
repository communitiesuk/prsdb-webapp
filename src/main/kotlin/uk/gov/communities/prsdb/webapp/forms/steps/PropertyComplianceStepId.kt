package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING

enum class PropertyComplianceStepId(
    override val urlPathSegment: String,
) : StepId {
    GasSafety("gas-safety-certificate"),
    GasSafetyIssueDate("gas-safety-certificate-issue-date"),
    GasSafetyEngineerNum("gas-safety-certificate-engineer-number"),
    GasSafetyUpload("gas-safety-certificate-$FILE_UPLOAD_URL_SUBSTRING"),
    GasSafetyUploadConfirmation("gas-safety-certificate-upload-confirmation"),
    GasSafetyOutdated("gas-safety-certificate-outdated"),
    GasSafetyExemption("gas-safety-certificate-exemption"),
    GasSafetyExemptionReason("gas-safety-certificate-exemption-reason"),
    GasSafetyExemptionOtherReason("gas-safety-certificate-exemption-other-reason"),
    GasSafetyExemptionConfirmation("gas-safety-certificate-exemption-confirmation"),
    GasSafetyExemptionMissing("gas-safety-certificate-exemption-missing"),
    EICR("eicr"),
    EicrIssueDate("eicr-issue-date"),
    EicrUpload("eicr-$FILE_UPLOAD_URL_SUBSTRING"),
    EicrOutdated("eicr-outdated"),
    EicrExemption("eicr-exemption"),
    EicrExemptionReason("eicr-exemption-reason"),
    EicrExemptionMissing("eicr-exemption-missing"),
    EPC("epc"),
    CheckAndSubmit("check-and-submit"),
    Declaration("declaration"),
}
