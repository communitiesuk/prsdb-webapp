package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.toNormalizedIntegerString
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class NumberOfHouseholdsFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.numberOfHouseholds.restructureAndSkipping.input.error",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "numberOfHouseholdsIsValidForAction",
            ),
        ],
    )
    var numberOfHouseholds: String = ""
        set(value) {
            field = value.toNormalizedIntegerString()
        }

    var action: String? = null

    fun numberOfHouseholdsIsValidForAction(): Boolean =
        action == PROVIDE_THIS_LATER_BUTTON_ACTION_NAME || numberOfHouseholds.toIntOrNull()?.let { it > 0 } == true

    companion object {
        fun fromPropertyOwnership(propertyOwnership: PropertyOwnership): NumberOfHouseholdsFormModel =
            NumberOfHouseholdsFormModel().apply { numberOfHouseholds = propertyOwnership.currentNumHouseholds.toString() }
    }
}
