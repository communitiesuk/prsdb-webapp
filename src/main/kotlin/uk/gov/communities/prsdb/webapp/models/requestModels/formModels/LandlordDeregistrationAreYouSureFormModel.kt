package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class LandlordDeregistrationAreYouSureFormModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.areYouSure.landlordDeregistration.noProperties.radios.error.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isNotNullWhenLandlordHasNoProperties",
            ),
            ConstraintDescriptor(
                messageKey = "forms.areYouSure.landlordDeregistration.withProperties.radios.error.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isNotNullWhenLandlordHasRegisteredProperties",
            ),
        ],
    )
    var wantsToProceed: Boolean? = null,
    var landlordHasProperties: Boolean? = null,
) : FormModel {
    fun isNotNullWhenLandlordHasRegisteredProperties(): Boolean =
        (NotNullValidator().isValid(wantsToProceed, null) && landlordHasProperties == true) ||
            landlordHasProperties == false

    fun isNotNullWhenLandlordHasNoProperties(): Boolean =
        (NotNullValidator().isValid(wantsToProceed, null) && landlordHasProperties == false) ||
            landlordHasProperties == true
}
