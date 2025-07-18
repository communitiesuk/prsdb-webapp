package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class SelectAddressFormModel : FormModel {
    @NotNull(message = "forms.selectAddress.error.missing")
    var address: String? = null

    companion object {
        fun fromLandlord(landlord: Landlord): SelectAddressFormModel =
            SelectAddressFormModel().apply { address = landlord.address.getSelectedAddress() }
    }
}
