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
                messageKey = "deregisterLandlord.noProperties.areYouSure.radios.error.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isNotNullWhenLandlordHasNoProperties",
            ),
        ],
    )
    var wantsToProceed: Boolean? = null,
    var userHasRegisteredProperties: Boolean? = null,
) : FormModel {
    fun isNotNullWhenLandlordHasNoProperties(): Boolean =
        NotNullValidator().isValid(wantsToProceed, null) || userHasRegisteredProperties == true
}
