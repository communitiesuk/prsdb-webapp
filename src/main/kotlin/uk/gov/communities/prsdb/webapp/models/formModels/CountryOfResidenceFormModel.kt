package uk.gov.communities.prsdb.webapp.models.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class CountryOfResidenceFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.countryOfResidence.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var livesInUK: Boolean? = null

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.countryOfResidence.radios.option.no.select.error.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isCountryOfResidenceValid",
            ),
        ],
    )
    var countryOfResidence: String = ""

    fun isCountryOfResidenceValid(): Boolean = livesInUK != false || countryOfResidence.isNotBlank()
}
