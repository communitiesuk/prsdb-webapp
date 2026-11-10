package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindCheckboxOption
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class OwnershipKindFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.ownershipKind.error.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isSelectionValid",
            ),
        ],
    )
    var ownershipKinds: MutableList<String?> = mutableListOf()

    fun isSelectionValid(): Boolean {
        val selectedValues = nonBlankOwnershipKinds()
        return selectedValues.isNotEmpty() &&
            selectedValues.all { value -> OwnershipKindCheckboxOption.entries.any { it.name == value } }
    }

    fun toOwnershipKindCheckboxOptions(): Set<OwnershipKindCheckboxOption> =
        nonBlankOwnershipKinds().map { OwnershipKindCheckboxOption.valueOf(it) }.toSet()

    fun toOwnershipKind(): OwnershipKind? {
        val selectedOptions = toOwnershipKindCheckboxOptions()
        return OwnershipKind.entries.find { it.options == selectedOptions }
    }

    private fun nonBlankOwnershipKinds(): List<String> = ownershipKinds.filterNotNull().filter { it.isNotBlank() }
}
