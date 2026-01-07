package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class RentFrequencyFormModel : FormModel {
    @NotNull(message = "forms.rentFrequency.radios.error.missing")
    var rentFrequency: RentFrequency? = null

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.rentFrequency.radios.option.other.input.error.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isCustomRentFrequencyValid",
            ),
        ],
    )
    var customRentFrequency: String = ""

    fun isCustomRentFrequencyValid(): Boolean = rentFrequency != RentFrequency.OTHER || customRentFrequency.isNotBlank()
}
