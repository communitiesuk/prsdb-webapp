package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.toNormalizedIntegerString
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.PositiveIntegerValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class NumberOfPeopleFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.numberOfPeople.input.error.invalidFormat",
                validatorType = PositiveIntegerValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.numberOfPeople.input.error.invalidNumber",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isNotLessThanNumberOfHouseholds",
            ),
        ],
    )
    var numberOfPeople: String = ""
        set(value) {
            field = value.toNormalizedIntegerString()
        }

    var numberOfHouseholds: String = ""

    fun isNotLessThanNumberOfHouseholds(): Boolean = numberOfPeople.toInt() >= numberOfHouseholds.toInt()

    companion object {
        fun fromPropertyOwnership(propertyOwnership: PropertyOwnership): NumberOfPeopleFormModel =
            NumberOfPeopleFormModel().apply {
                numberOfPeople = propertyOwnership.tenancyDetails.currentNumTenants.toString()
                numberOfHouseholds = propertyOwnership.tenancyDetails.currentNumHouseholds.toString()
            }
    }
}

@IsValidPrioritised
class NewNumberOfPeopleFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.numberOfPeople.input.error.invalidFormat",
                validatorType = PositiveIntegerValidator::class,
            ),
        ],
    )
    var numberOfPeople: String = ""
        set(value) {
            field = value.toNormalizedIntegerString()
        }

    companion object {
        fun fromPropertyOwnership(propertyOwnership: PropertyOwnership): NewNumberOfPeopleFormModel =
            NewNumberOfPeopleFormModel().apply {
                numberOfPeople = propertyOwnership.tenancyDetails.currentNumTenants.toString()
            }
    }
}
