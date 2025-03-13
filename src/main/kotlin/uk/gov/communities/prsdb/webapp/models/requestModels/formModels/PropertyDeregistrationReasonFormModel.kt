package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.LengthConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class PropertyDeregistrationReasonFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.reason.propertyDeregistration.error.tooLong",
                validatorType = LengthConstraintValidator::class,
                validatorArgs = arrayOf("0", "200"),
            ),
        ],
    )
    var reason: String = ""
}
