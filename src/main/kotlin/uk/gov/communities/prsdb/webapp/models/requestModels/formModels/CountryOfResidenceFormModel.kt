package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class CountryOfResidenceFormModel : FormModel {
    @NotNull(message = "forms.countryOfResidence.radios.error.missing")
    var livesInEnglandOrWales: Boolean? = null

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

    fun isCountryOfResidenceValid(): Boolean = livesInEnglandOrWales != false || countryOfResidence.isNotBlank()
}
