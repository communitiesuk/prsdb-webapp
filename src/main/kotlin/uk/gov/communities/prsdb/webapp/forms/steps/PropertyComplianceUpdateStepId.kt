package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING

enum class PropertyComplianceUpdateStepId(
    override val urlPathSegment: String,
    override val groupIdentifier: PropertyComplianceUpdateGroupIdentifier,
    override val isCheckYourAnswersStepId: Boolean = false,
) : GroupedUpdateStepId<PropertyComplianceUpdateGroupIdentifier> {
    UpdateGasSafety("update-gas-safety-certificate", PropertyComplianceUpdateGroupIdentifier.GasSafety),
    GasSafetyUpload("gas-safety-certificate-$FILE_UPLOAD_URL_SUBSTRING", PropertyComplianceUpdateGroupIdentifier.GasSafety),
}

enum class PropertyComplianceUpdateGroupIdentifier {
    GasSafety,
}
