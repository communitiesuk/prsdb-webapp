package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class SelectAddressFormModel : FormModel {
    @NotNull(message = "forms.selectAddress.error.missing")
    var address: String? = null
}
