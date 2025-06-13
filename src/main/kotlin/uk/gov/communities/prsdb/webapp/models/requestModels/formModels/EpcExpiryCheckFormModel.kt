package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class EpcExpiryCheckFormModel : FormModel {
    @NotNull(message = "forms.epcExpiryCheck.missing")
    var tenancyStartedBeforeExpiry: Boolean? = null
}
