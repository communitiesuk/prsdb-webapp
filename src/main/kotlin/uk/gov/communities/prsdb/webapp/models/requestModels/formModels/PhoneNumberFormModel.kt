package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.PhoneNumberConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class PhoneNumberFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.phoneNumber.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.phoneNumber.error.invalidFormat",
                validatorType = PhoneNumberConstraintValidator::class,
            ),
        ],
    )
    var phoneNumber: String? = null

    companion object {
        fun fromLandlord(landlord: Landlord): PhoneNumberFormModel = PhoneNumberFormModel().apply { phoneNumber = landlord.phoneNumber }
    }
}
