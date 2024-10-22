package uk.gov.communities.prsdb.webapp.validation.validators

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uk.gov.communities.prsdb.webapp.models.dataModels.ConfirmedEmailDataModel
import uk.gov.communities.prsdb.webapp.validation.constraints.ValidConfirmedEmail

class ConfirmedEmailValidator : ConstraintValidator<ValidConfirmedEmail, ConfirmedEmailDataModel> {
    override fun isValid(
        value: ConfirmedEmailDataModel?,
        context: ConstraintValidatorContext?,
    ): Boolean = value!!.confirmEmail == value.email
}
