package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class PropertyTypeFormModel : FormModel {
    @NotNull(message = "forms.propertyType.radios.error.missing")
    var propertyType: PropertyType? = null

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.propertyType.radios.option.other.input.error.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isCustomPropertyTypeValid",
            ),
        ],
    )
    var customPropertyType: String = ""

    fun isCustomPropertyTypeValid(): Boolean = propertyType != PropertyType.OTHER || customPropertyType.isNotBlank()
}
