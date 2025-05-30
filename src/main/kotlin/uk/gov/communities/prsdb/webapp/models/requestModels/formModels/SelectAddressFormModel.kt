package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class SelectAddressFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.selectAddress.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var address: String? = null

    companion object {
        fun fromLandlord(landlord: Landlord): SelectAddressFormModel =
            SelectAddressFormModel().apply { address = landlord.address.getSelectedAddress() }
    }
}
