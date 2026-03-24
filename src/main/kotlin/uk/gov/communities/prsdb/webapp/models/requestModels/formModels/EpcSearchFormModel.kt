package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

// TODO PDJB-662 - remove if not needed or implement properly if needed, this is a placeholder for the radios version of the EPC search form
class EpcSearchFormModel : FormModel {
    @NotNull(message = "forms.epcSearch.error.missing")
    var epcSearchMode: String? = null
}
