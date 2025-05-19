package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class EpcLookupFormModel : FormModel {
    @NotNull(message = "forms.epcLookup.error.missing")
    var certificateNumber: String? = null
}
