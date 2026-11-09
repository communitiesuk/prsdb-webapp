package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindOption
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

    fun isSelectionValid(): Boolean = toOwnershipKind() != null

    fun toOwnershipKind(): OwnershipKind? {
        val selected = ownershipKinds.filterNotNull().filter { it.isNotBlank() }.toSet()
        val options = OwnershipKindOption.entries.filter { it.name in selected }.toSet()
        if (options.size != selected.size) return null
        return OwnershipKind.entries.find { it.options == options }
    }
}
