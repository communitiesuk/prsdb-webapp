package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class ConfirmEpcDetailsFromCertificateNumberFormModel : MatchedEpcFormModel {
    @NotNull(message = "propertyCompliance.epcTask.confirmEpcDetailsFromCertificateNumber.error.missing")
    override var matchedEpcIsCorrect: Boolean? = null
}
