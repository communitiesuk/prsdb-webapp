package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class LookupAddressFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.lookupAddress.postcode.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
        ],
    )
    var postcode: String? = null

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.lookupAddress.houseNameOrNumber.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
        ],
    )
    var houseNameOrNumber: String? = null

    companion object {
        fun fromLandlord(landlord: Landlord): LookupAddressFormModel =
            LookupAddressFormModel().apply {
                // We default to singleLineAddress not because that's useful data, but because we want this form to
                // be considered valid, and thus its journey Step to be satisfied, in the case that a manual address was
                // provided rather than address lookup terms.
                postcode = landlord.address.postcode ?: landlord.address.singleLineAddress
                houseNameOrNumber = landlord.address.buildingName ?: landlord.address.buildingNumber ?: landlord.address.singleLineAddress
            }
    }
}
