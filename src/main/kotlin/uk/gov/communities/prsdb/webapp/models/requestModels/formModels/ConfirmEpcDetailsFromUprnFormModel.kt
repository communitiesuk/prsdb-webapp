package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class ConfirmEpcDetailsFromUprnFormModel : MatchedEpcFormModel {
    @NotNull(message = "propertyCompliance.epcTask.confirmEpcDetailsFromUprn.error.missing")
    override var matchedEpcIsCorrect: Boolean? = null
}
