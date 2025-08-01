package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class UpdateEicrFormModel : FormModel {
    @NotNull(message = "forms.update.eicr.error.missing")
    var hasNewCertificate: Boolean? = null
}
